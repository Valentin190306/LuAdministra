package com.luadministra.ml;

import com.luadministra.compra.Compra;
import com.luadministra.compra.CompraRepository;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MLService {

    private static final Pattern ML_ID_PATTERN = Pattern.compile("^M[A-Z]{2,}[0-9]+$");
    private static final String ML_API_BASE = "https://api.mercadolibre.com/items/";
    private static final String BROWSER_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final CompraRepository compraRepository;
    private final ConsultaPrecioMLRepository consultaRepository;
    private final HttpClient httpClient;

    public MLService(CompraRepository compraRepository,
                     ConsultaPrecioMLRepository consultaRepository) {
        this.compraRepository = compraRepository;
        this.consultaRepository = consultaRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public List<ConsultaPrecioMLResponse> listarConsultas(Long compraId) {
        return consultaRepository.findByCompraIdOrderByFechaHoraDesc(compraId)
                .stream()
                .map(ConsultaPrecioMLResponse::fromEntity)
                .toList();
    }

    public List<CompraConMLResponse> listarComprasConML() {
        return compraRepository.findByMlIdIsNotNullOrderByFechaDesc()
                .stream()
                .map(c -> {
                    ConsultaPrecioML ultima = consultaRepository
                            .findFirstByCompraIdOrderByFechaHoraDesc(c.getId())
                            .orElse(null);
                    return new CompraConMLResponse(
                            c.getId(),
                            c.getMateriaPrima().getId(),
                            c.getMateriaPrima().getNombre(),
                            c.getFecha(),
                            c.getPrecio(),
                            c.getLugar(),
                            c.getMlId(),
                            ultima != null ? ultima.getPrecio() : null,
                            ultima != null ? ultima.getFechaHora() : null
                    );
                })
                .toList();
    }

    @Transactional
    public ConsultaPrecioMLResponse consultarPrecio(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra no encontrada"));

        String mlId = compra.getMlId();
        if (mlId == null || mlId.isBlank()) {
            throw new SolicitudInvalidaException("La compra no tiene un ID de Mercado Libre asociado");
        }

        if (!ML_ID_PATTERN.matcher(mlId).matches()) {
            throw new SolicitudInvalidaException("El ID de Mercado Libre tiene un formato invalido: " + mlId);
        }

        Double precio = consultarPrecioML(mlId);

        ConsultaPrecioML consulta = new ConsultaPrecioML();
        consulta.setCompra(compra);
        consulta.setPrecio(precio);
        consulta.setFechaHora(LocalDateTime.now());

        return ConsultaPrecioMLResponse.fromEntity(consultaRepository.save(consulta));
    }

    private Double consultarPrecioML(String mlId) {
        try {
            return consultarApiML(mlId);
        } catch (Exception e) {
            try {
                return consultarWebML(mlId);
            } catch (Exception e2) {
                throw new SolicitudInvalidaException(
                        "Error al consultar Mercado Libre:\nAPI: " + e.getMessage() + "\nWeb: " + e2.getMessage());
            }
        }
    }

    private Double consultarApiML(String mlId) {
        try {
            String url = ML_API_BASE + mlId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("User-Agent", BROWSER_UA)
                    .header("Accept", "application/json")
                    .header("Referer", "https://www.mercadolibre.com.ar/")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 403) {
                throw new RuntimeException("La API de Mercado Libre requiere autenticacion (HTTP 403)");
            }
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error al consultar Mercado Libre: codigo " + response.statusCode());
            }

            Double precio = extraerPrecioJSON(response.body());
            if (precio == null) {
                throw new RuntimeException("No se pudo obtener el precio de la respuesta de Mercado Libre");
            }
            return precio;
        } catch (java.net.http.HttpConnectTimeoutException e) {
            throw new RuntimeException("Tiempo de conexion agotado al consultar Mercado Libre");
        } catch (java.net.http.HttpTimeoutException e) {
            throw new RuntimeException("Tiempo de lectura agotado al consultar Mercado Libre");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar Mercado Libre: " + e.getMessage());
        }
    }

    private static String dominioML(String mlId) {
        String prefix = mlId.replaceAll("\\d.*", "");
        return switch (prefix) {
            case "MLU" -> "mercadolibre.com.uy";
            case "MLC" -> "mercadolibre.cl";
            case "MCO" -> "mercadolibre.com.co";
            case "MLM" -> "mercadolibre.com.mx";
            case "MPE" -> "mercadolibre.com.pe";
            case "MEC" -> "mercadolibre.com.ec";
            case "MLV" -> "mercadolibre.com.ve";
            case "MPA" -> "mercadolibre.com.pa";
            case "MCR" -> "mercadolibre.co.cr";
            case "MDO" -> "mercadolibre.com.do";
            case "MSV" -> "mercadolibre.com.sv";
            case "MGT" -> "mercadolibre.com.gt";
            case "MHN" -> "mercadolibre.com.hn";
            case "MNI" -> "mercadolibre.com.ni";
            case "MPY" -> "mercadolibre.com.py";
            case "MLB" -> "mercadolivre.com.br";
            default -> "mercadolibre.com.ar";
        };
    }

    private Double consultarWebML(String mlId) {
        String dom = dominioML(mlId);
        List<String> urls = List.of(
                "https://www." + dom + "/p/" + mlId,
                "https://www." + dom + "/item/" + mlId);

        List<String> errores = new ArrayList<>();
        for (String url : urls) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(TIMEOUT)
                        .header("User-Agent", BROWSER_UA)
                        .header("Accept", "text/html,application/xhtml+xml")
                        .header("Accept-Language", "es-AR,es;q=0.9")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    errores.add(url + " -> HTTP " + response.statusCode());
                    continue;
                }

                Double precio = extraerPrecioHTML(response.body());
                if (precio != null) return precio;

                errores.add(url + " -> precio no encontrado en HTML");
            } catch (Exception e) {
                errores.add(url + " -> " + e.getMessage());
            }
        }

        throw new RuntimeException(String.join(" | ", errores));
    }

    private static Double extraerPrecioJSON(String jsonBody) {
        try {
            String key = "\"price\"";
            int idx = jsonBody.indexOf(key);
            if (idx < 0) return null;
            int start = idx + key.length() + 1;
            while (start < jsonBody.length() && jsonBody.charAt(start) == ' ') start++;
            if (start >= jsonBody.length()) return null;
            StringBuilder num = new StringBuilder();
            boolean inNumber = false;
            for (int i = start; i < jsonBody.length(); i++) {
                char c = jsonBody.charAt(i);
                if (Character.isDigit(c) || c == '.') {
                    num.append(c);
                    inNumber = true;
                } else if (inNumber) {
                    break;
                }
            }
            return num.length() > 0 ? Double.parseDouble(num.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Double extraerPrecioHTML(String html) {
        try {
            // 1 — JSON-LD structured data blocks
            Pattern jsonld = Pattern.compile(
                    "<script\\s+type=\"application/ld\\+json\">(.*?)</script>",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = jsonld.matcher(html);
            while (m.find()) {
                Double p = extraerPrecioDeJSON(m.group(1));
                if (p != null) return p;
            }

            // 2 — __NEXT_DATA__
            Pattern nextData = Pattern.compile(
                    "<script\\s+id=\"__NEXT_DATA__\"\\s+type=\"application/json\"(?:>|\\s*>)(.*?)</script>",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            m = nextData.matcher(html);
            if (m.find()) {
                Double p = extraerPrecioDeJSON(m.group(1));
                if (p != null) return p;
            }

            // 3 — explicit patterns: "price" key anywhere in the page
            Pattern priceKey = Pattern.compile("\"price\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?");
            m = priceKey.matcher(html);
            if (m.find()) return Double.parseDouble(m.group(1));

            // 4 — andes-money-amount__fraction
            Pattern fraction = Pattern.compile("andes-money-amount__fraction[^>]*>([0-9.,]+)<");
            m = fraction.matcher(html);
            if (m.find()) {
                return Double.parseDouble(m.group(1).replace(".", "").replace(",", "."));
            }

            // 5 — meta itemprop="price"
            Pattern metaPrice = Pattern.compile(
                    "<meta\\s+itemprop=\"price\"\\s+content=\"([0-9.]+)\"");
            m = metaPrice.matcher(html);
            if (m.find()) return Double.parseDouble(m.group(1));

            // 6 — data-price attribute
            Pattern dataPrice = Pattern.compile("data-price=\"([0-9.]+)\"");
            m = dataPrice.matcher(html);
            if (m.find()) return Double.parseDouble(m.group(1));

            // 7 — $ followed by number (visible text price like "$ 19.949")
            Pattern precioTexto = Pattern.compile(
                    "\\$\\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]+)?)");
            m = precioTexto.matcher(html);
            if (m.find()) {
                String raw = m.group(1);
                if (raw.contains(",") && raw.contains(".")) {
                    raw = raw.replace(".", "").replace(",", ".");
                } else if (raw.contains(",")) {
                    raw = raw.replace(",", ".");
                } else if (raw.contains(".") && raw.length() > 3) {
                    raw = raw.replace(".", "");
                }
                return Double.parseDouble(raw);
            }

            // 8 — aria-label containing "Precio" + number
            Pattern ariaPrecio = Pattern.compile("Precio[^0-9]*\\$?\\s*([0-9]+(?:[.,][0-9]+)?)");
            m = ariaPrecio.matcher(html);
            if (m.find()) {
                String val = m.group(1).replace(".", "").replace(",", ".");
                return Double.parseDouble(val);
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Double extraerPrecioDeJSON(String json) {
        Pattern p = Pattern.compile("\"price\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return null;
    }
}
