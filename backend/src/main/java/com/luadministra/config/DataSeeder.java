package com.luadministra.config;

import com.luadministra.categoria.Categoria;
import com.luadministra.categoria.CategoriaRepository;
import com.luadministra.categoria.TipoCategoria;
import com.luadministra.consignacion.Consignacion;
import com.luadministra.consignacion.ConsignacionRepository;
import com.luadministra.consignacion.EstadoConsignacion;
import com.luadministra.consignacion.LineaConsignacion;
import com.luadministra.consignatario.Consignatario;
import com.luadministra.consignatario.ConsignatarioRepository;
import com.luadministra.compra.Compra;
import com.luadministra.compra.CompraRepository;
import com.luadministra.lote.Lote;
import com.luadministra.lote.LoteRepository;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.receta.Receta;
import com.luadministra.receta.RecetaDetalle;
import com.luadministra.receta.RecetaRepository;
import com.luadministra.rendicion.Rendicion;
import com.luadministra.rendicion.RendicionRepository;
import com.luadministra.venta.LineaVenta;
import com.luadministra.venta.Venta;
import com.luadministra.venta.VentaRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("default")
public class DataSeeder {

    private final MateriaPrimaRepository materiaPrimaRepo;
    private final ProductoRepository productoRepo;
    private final RecetaRepository recetaRepo;
    private final CompraRepository compraRepo;
    private final LoteRepository loteRepo;
    private final VentaRepository ventaRepo;
    private final CategoriaRepository categoriaRepo;
    private final ConsignatarioRepository consignatarioRepo;
    private final ConsignacionRepository consignacionRepo;
    private final RendicionRepository rendicionRepo;

    public DataSeeder(
            MateriaPrimaRepository materiaPrimaRepo,
            ProductoRepository productoRepo,
            RecetaRepository recetaRepo,
            CompraRepository compraRepo,
            LoteRepository loteRepo,
            VentaRepository ventaRepo,
            CategoriaRepository categoriaRepo,
            ConsignatarioRepository consignatarioRepo,
            ConsignacionRepository consignacionRepo,
            RendicionRepository rendicionRepo) {
        this.materiaPrimaRepo = materiaPrimaRepo;
        this.productoRepo = productoRepo;
        this.recetaRepo = recetaRepo;
        this.compraRepo = compraRepo;
        this.loteRepo = loteRepo;
        this.ventaRepo = ventaRepo;
        this.categoriaRepo = categoriaRepo;
        this.consignatarioRepo = consignatarioRepo;
        this.consignacionRepo = consignacionRepo;
        this.rendicionRepo = rendicionRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (materiaPrimaRepo.count() > 0) return;

        var aceites = cat("Aceites y Grasas", null, TipoCategoria.MATERIA_PRIMA);
        var esenciales = cat("Aceites Esenciales", aceites, TipoCategoria.MATERIA_PRIMA);
        var polvos = cat("Polvos y Arcillas", null, TipoCategoria.MATERIA_PRIMA);
        var ceras = cat("Ceras", null, TipoCategoria.MATERIA_PRIMA);
        var harinas = cat("Harinas y Almidones", polvos, TipoCategoria.MATERIA_PRIMA);

        var shampooCat = cat("Shampoos", null, TipoCategoria.PRODUCTO);
        var cuidadoBucal = cat("Cuidado Bucal", null, TipoCategoria.PRODUCTO);
        var desodorantesCat = cat("Desodorantes", null, TipoCategoria.PRODUCTO);
        var rostro = cat("Rostro y Cuidado Facial", null, TipoCategoria.PRODUCTO);

        var karite = mp("Manteca de Karité", "g", 500.0, 200.0, aceites);
        var coco = mp("Aceite de Coco", "ml", 300.0, 150.0, aceites);
        var bicarbonato = mp("Bicarbonato de Sodio", "g", 1000.0, 500.0, polvos);
        var arcilla = mp("Arcilla Blanca", "g", 800.0, 300.0, polvos);
        var menta = mp("Aceite Esencial de Menta", "ml", 50.0, 20.0, esenciales);
        var carnauba = mp("Cera de Carnaúba", "g", 200.0, 100.0, ceras);
        var maiz = mp("Almidón de Maíz", "g", 1500.0, 500.0, harinas);
        var oliva = mp("Aceite de Oliva", "ml", 200.0, 100.0, aceites);
        var almendras = mp("Aceite de Almendras", "ml", 150.0, 75.0, aceites);
        var lavanda = mp("Aceite Esencial de Lavanda", "ml", 40.0, 15.0, esenciales);
        var mantecaCacao = mp("Manteca de Cacao", "g", 300.0, 120.0, aceites);
        var caliza = mp("Carbonato de Calcio", "g", 600.0, 250.0, polvos);

        var shampooNormal = pt("Shampoo en Barra - Cabello Normal", 850.0, 8.0, 5.0, shampooCat);
        var shampooGraso = pt("Shampoo en Barra - Cabello Graso", 850.0, 3.0, 5.0, shampooCat);
        var pastaDental = pt("Pasta Dental Natural", 650.0, 7.0, 5.0, cuidadoBucal);
        var desodorante = pt("Desodorante en Barra", 600.0, 2.0, 5.0, desodorantesCat);
        var jabonFacial = pt("Jabón Facial Natural", 450.0, 12.0, 5.0, rostro);
        var shampooSeco = pt("Shampoo en Barra - Cabello Seco", 850.0, 0.0, 5.0, shampooCat);
        var balsamoLabial = pt("Bálsamo Labial Natural", 350.0, 0.0, 3.0, rostro);

        receta(shampooNormal, "Batir la manteca de karité a temperatura ambiente antes de mezclar.",
                det(karite, 50.0), det(coco, 30.0), det(menta, 2.0), det(carnauba, 10.0));

        receta(shampooGraso, "Aumentar la cantidad de menta si se desea efecto refrescante.",
                det(arcilla, 40.0), det(coco, 20.0), det(menta, 3.0), det(maiz, 10.0));

        receta(pastaDental, "Mezclar los aceites esenciales al final para evitar evaporación.",
                det(bicarbonato, 30.0), det(coco, 15.0), det(menta, 1.0), det(caliza, 20.0));

        receta(desodorante, "Requiere reposo en heladera por 2 horas antes de desmoldar.",
                det(karite, 25.0), det(bicarbonato, 15.0), det(maiz, 10.0), det(menta, 1.0));

        receta(jabonFacial, null,
                det(arcilla, 20.0), det(oliva, 30.0), det(almendras, 10.0));

        receta(shampooSeco, "Versión suave para cuero cabelludo sensible.",
                det(mantecaCacao, 40.0), det(almendras, 25.0), det(lavanda, 2.0), det(carnauba, 8.0));

        receta(balsamoLabial, "Envasar en caliente y dejar enfriar 24h antes de usar.",
                det(mantecaCacao, 15.0), det(carnauba, 5.0), det(almendras, 10.0));

        comprar(karite, LocalDate.of(2024, 7, 10), 1000.0, 1400.0, "Mercado Central", null, null);
        comprar(karite, LocalDate.of(2025, 1, 15), 1000.0, 1500.0, "Mercado Central", "https://mercadocentral.example.com/manteca-karite", null);
        comprar(karite, LocalDate.of(2025, 3, 20), 1000.0, 1800.0, "Mercado Central", null, null);
        comprar(karite, LocalDate.of(2025, 9, 5), 1000.0, 2200.0, "Distribuidora Química", "https://distribuidora-quimica.example.com/producto/123", null);
        comprar(karite, LocalDate.of(2026, 2, 10), 1000.0, 2500.0, "Mercado Central", null, null);
        comprar(coco, LocalDate.of(2025, 2, 1), 500.0, 800.0, "El Herbolario", null, null);
        comprar(coco, LocalDate.of(2025, 8, 15), 500.0, 950.0, "El Herbolario", null, null);
        comprar(coco, LocalDate.of(2026, 1, 20), 500.0, 1100.0, "El Herbolario", null, null);
        comprar(bicarbonato, LocalDate.of(2025, 1, 10), 2000.0, 400.0, "Distribuidora Química", null, null);
        comprar(bicarbonato, LocalDate.of(2025, 11, 20), 2000.0, 450.0, "Distribuidora Química", null, null);
        comprar(menta, LocalDate.of(2025, 2, 15), 100.0, 2500.0, "Aromas Naturales", null, null);
        comprar(menta, LocalDate.of(2025, 7, 1), 100.0, 2750.0, "Aromas Naturales", null, null);
        comprar(arcilla, LocalDate.of(2025, 3, 1), 1000.0, 600.0, "El Herbolario", null, null);
        comprar(arcilla, LocalDate.of(2025, 10, 10), 1000.0, 650.0, "El Herbolario", null, null);
        comprar(carnauba, LocalDate.of(2025, 4, 15), 500.0, 1800.0, "Distribuidora Química", "https://distribuidora-quimica.example.com/cera-carnauba", null);
        comprar(oliva, LocalDate.of(2025, 5, 10), 500.0, 1200.0, null, null, null);
        comprar(almendras, LocalDate.of(2025, 6, 1), 300.0, 1500.0, "El Herbolario", null, null);
        comprar(lavanda, LocalDate.of(2025, 6, 15), 80.0, 3200.0, "Aromas Naturales", "https://aromas-naturales.example.com/lavanda", null);
        comprar(mantecaCacao, LocalDate.of(2025, 7, 20), 500.0, 1600.0, "Mercado Central", null, null);
        comprar(caliza, LocalDate.of(2025, 8, 1), 800.0, 350.0, "Distribuidora Química", null, null);
        comprar(mantecaCacao, LocalDate.of(2026, 3, 5), 500.0, 1900.0, "El Herbolario", null, null);
        comprar(lavanda, LocalDate.of(2026, 4, 1), 100.0, 3400.0, "Aromas Naturales", "https://aromas-naturales.example.com/lavanda", null);

        producir(shampooNormal, LocalDate.of(2025, 3, 1), 10.0, 30);
        producir(shampooGraso, LocalDate.of(2025, 3, 1), 5.0, 30);
        producir(pastaDental, LocalDate.of(2025, 3, 5), 8.0, 30);
        producir(desodorante, LocalDate.of(2025, 3, 5), 5.0, 30);
        producir(jabonFacial, LocalDate.of(2025, 3, 10), 12.0, 30);
        producir(shampooNormal, LocalDate.of(2025, 6, 1), 8.0, 90);
        producir(desodorante, LocalDate.of(2025, 7, 1), 6.0, 90);
        producir(shampooGraso, LocalDate.of(2025, 9, 15), 5.0, 120);
        producir(pastaDental, LocalDate.of(2025, 11, 1), 10.0, 120);
        producir(shampooSeco, LocalDate.of(2025, 8, 1), 6.0, 90);
        producir(balsamoLabial, LocalDate.of(2025, 8, 15), 10.0, 90);
        producir(shampooNormal, LocalDate.of(2026, 1, 10), 12.0, null);
        producir(jabonFacial, LocalDate.of(2026, 2, 15), 10.0, null);
        producir(desodorante, LocalDate.of(2026, 3, 20), 8.0, null);

        vender(LocalDate.of(2025, 3, 15), item(shampooNormal, 2.0), item(desodorante, 3.0));
        vender(LocalDate.of(2025, 3, 20), item(shampooGraso, 2.0));
        vender(LocalDate.of(2025, 3, 22), item(pastaDental, 1.0));
        vender(LocalDate.of(2025, 4, 5), item(jabonFacial, 2.0));
        vender(LocalDate.of(2025, 6, 15), item(shampooNormal, 3.0));
        vender(LocalDate.of(2025, 7, 20), item(desodorante, 2.0));
        vender(LocalDate.of(2025, 9, 1), item(shampooNormal, 1.5));
        vender(LocalDate.of(2025, 9, 10), item(shampooSeco, 2.0));
        vender(LocalDate.of(2025, 9, 15), item(balsamoLabial, 4.0));
        vender(LocalDate.of(2025, 11, 15), item(pastaDental, 3.0));
        vender(LocalDate.of(2025, 12, 5), item(shampooGraso, 1.0));
        vender(LocalDate.of(2026, 1, 20), item(shampooSeco, 2.5));
        vender(LocalDate.of(2026, 2, 1), item(shampooNormal, 3.0), item(balsamoLabial, 2.0));
        vender(LocalDate.of(2026, 3, 10), item(balsamoLabial, 3.0));
        vender(LocalDate.of(2026, 4, 5), item(jabonFacial, 2.0), item(desodorante, 2.0));
        vender(LocalDate.of(2026, 4, 15), item(desodorante, 1.0), item(pastaDental, 2.0), item(shampooNormal, 2.0));

        // Consignatarios
        var maria = consig("María González", "maria@email.com");
        var laura = consig("Laura Rodríguez", null);
        var ana = consig("Ana Martínez", "11-5555-1234");

        // Consignaciones (no descuentan stock, solo registran)
        var consig1 = consignacion(maria, LocalDate.of(2026, 3, 1),
                new ConsignacionLinea(shampooNormal, 10.0));
        var consig2 = consignacion(laura, LocalDate.of(2026, 3, 10),
                new ConsignacionLinea(pastaDental, 15.0));
        var consig3 = consignacion(ana, LocalDate.of(2026, 3, 15),
                new ConsignacionLinea(desodorante, 8.0));
        var consig4 = consignacion(maria, LocalDate.of(2026, 4, 1),
                new ConsignacionLinea(shampooGraso, 5.0));

        // Rendiciones
        var lc1 = consig1.getLineas().get(0);
        var lc2 = consig2.getLineas().get(0);
        var lc3 = consig3.getLineas().get(0);
        var lc4 = consig4.getLineas().get(0);

        rendir(consig1, 5100.0, LocalDate.of(2026, 3, 20),
                new RendicionLinea(lc1, 6.0, 3.0));
        rendir(consig1, 850.0, LocalDate.of(2026, 4, 5),
                new RendicionLinea(lc1, 1.0, 0.0));

        rendir(consig2, 6500.0, LocalDate.of(2026, 4, 10),
                new RendicionLinea(lc2, 10.0, 3.0));
    }

    private record VentaItem(Producto p, double cantidad) {}
    private record ConsignacionLinea(Producto p, double cantidad) {}
    private record RendicionLinea(LineaConsignacion lc, double vendida, double devuelta) {}

    private VentaItem item(Producto p, double cantidad) {
        return new VentaItem(p, cantidad);
    }

    private Categoria cat(String nombre, Categoria padre, TipoCategoria tipo) {
        var c = new Categoria();
        c.setNombre(nombre);
        c.setCategoriaPadre(padre);
        c.setTipo(tipo);
        return categoriaRepo.save(c);
    }

    private MateriaPrima mp(String nombre, String unidad, double stock, Double stockMinimo, Categoria categoria) {
        var m = new MateriaPrima();
        m.setNombre(nombre);
        m.setUnidadMedida(unidad);
        m.setStockActual(stock);
        m.setStockMinimo(stockMinimo);
        m.setCategoria(categoria);
        return materiaPrimaRepo.save(m);
    }

    private Producto pt(String nombre, double precio, double stock, Double stockMinimo, Categoria categoria) {
        var p = new Producto();
        p.setNombre(nombre);
        p.setPrecioVenta(precio);
        p.setStockActual(stock);
        p.setStockMinimo(stockMinimo);
        p.setCategoria(categoria);
        return productoRepo.save(p);
    }

    private RecetaDetalle det(MateriaPrima mp, double cantidad) {
        var d = new RecetaDetalle();
        d.setMateriaPrima(mp);
        d.setCantidad(cantidad);
        return d;
    }

    private void receta(Producto p, String notas, RecetaDetalle... detalles) {
        var r = new Receta();
        r.setProducto(p);
        r.setNotas(notas);
        var lista = List.of(detalles);
        lista.forEach(d -> d.setReceta(r));
        r.setDetalles(lista);
        recetaRepo.save(r);
    }

    private void comprar(MateriaPrima mp, LocalDate fecha, double cantidad, double precio, String lugar, String url, Double precioMLReferencia) {
        var c = new Compra();
        c.setMateriaPrima(mp);
        c.setFecha(fecha);
        c.setCantidad(cantidad);
        c.setPrecio(precio);
        c.setLugar(lugar);
        c.setUrl(url);
        c.setPrecioMLReferencia(precioMLReferencia);
        compraRepo.save(c);
    }

    private void producir(Producto p, LocalDate fecha, double cantidad, Integer diasVigencia) {
        var l = new Lote();
        l.setProducto(p);
        l.setFecha(fecha);
        l.setCantidadFabricada(cantidad);
        l.setDiasVigencia(diasVigencia);
        loteRepo.save(l);
    }

    private Consignatario consig(String nombre, String contacto) {
        var c = new Consignatario();
        c.setNombre(nombre);
        c.setContacto(contacto);
        return consignatarioRepo.save(c);
    }

    private Consignacion consignacion(Consignatario consig, LocalDate fecha, ConsignacionLinea... lineas) {
        var c = new Consignacion();
        c.setConsignatario(consig);
        c.setFecha(fecha);
        for (var l : lineas) {
            var lc = new LineaConsignacion();
            lc.setConsignacion(c);
            lc.setProducto(l.p());
            lc.setCantidad(l.cantidad());
            lc.setPrecioUnitario(l.p().getPrecioVenta());
            c.getLineas().add(lc);
        }
        return consignacionRepo.save(c);
    }

    private void rendir(Consignacion consignacion, double monto, LocalDate fecha, RendicionLinea... lineas) {
        var r = new Rendicion();
        r.setConsignacion(consignacion);
        r.setMontoEntregado(monto);
        r.setFecha(fecha);
        for (var l : lineas) {
            var lr = new com.luadministra.rendicion.LineaRendicion();
            lr.setRendicion(r);
            lr.setLineaConsignacion(l.lc());
            lr.setCantidadVendida(l.vendida());
            lr.setCantidadDevuelta(l.devuelta());
            r.getLineas().add(lr);

            if (l.vendida() > 0) {
                l.lc().getProducto().setStockActual(l.lc().getProducto().getStockActual() - l.vendida());
            }
            if (l.devuelta() > 0) {
                l.lc().getProducto().setStockActual(l.lc().getProducto().getStockActual() + l.devuelta());
            }
            productoRepo.save(l.lc().getProducto());

            if (l.vendida() > 0) {
                var venta = new Venta();
                venta.setFecha(fecha);
                var vl = new LineaVenta();
                vl.setVenta(venta);
                vl.setProducto(l.lc().getProducto());
                vl.setCantidad(l.vendida());
                vl.setPrecioUnitario(l.lc().getPrecioUnitario());
                venta.setLineas(List.of(vl));
                ventaRepo.save(venta);
            }
        }
        rendicionRepo.save(r);

        double totalConsignado = consignacion.getLineas().stream()
                .mapToDouble(lc -> lc.getCantidad())
                .sum();
        var anteriores = rendicionRepo.findByConsignacionId(consignacion.getId());
        double totalRendido = anteriores.stream()
                .flatMap(rr -> rr.getLineas().stream())
                .mapToDouble(lr -> lr.getCantidadVendida() + lr.getCantidadDevuelta())
                .sum();
        if (totalRendido >= totalConsignado) {
            consignacion.setEstado(EstadoConsignacion.RENDIDO_TOTAL);
        } else {
            consignacion.setEstado(EstadoConsignacion.RENDIDO_PARCIAL);
        }
        consignacionRepo.save(consignacion);
    }

    private void vender(LocalDate fecha, VentaItem... items) {
        var v = new Venta();
        v.setFecha(fecha);
        var lineas = new ArrayList<LineaVenta>();
        for (var i : items) {
            var l = new LineaVenta();
            l.setVenta(v);
            l.setProducto(i.p());
            l.setCantidad(i.cantidad());
            l.setPrecioUnitario(i.p().getPrecioVenta());
            lineas.add(l);
        }
        v.setLineas(lineas);
        ventaRepo.save(v);
    }
}
