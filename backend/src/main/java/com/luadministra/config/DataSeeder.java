package com.luadministra.config;

import com.luadministra.categoriamateriaprima.CategoriaMateriaPrima;
import com.luadministra.categoriamateriaprima.CategoriaMateriaPrimaRepository;
import com.luadministra.categoriaproductoterminado.CategoriaProductoTerminado;
import com.luadministra.categoriaproductoterminado.CategoriaProductoTerminadoRepository;
import com.luadministra.colaboradora.Colaboradora;
import com.luadministra.colaboradora.ColaboradoraRepository;
import com.luadministra.compra.Compra;
import com.luadministra.compra.CompraRepository;
import com.luadministra.despacho.Despacho;
import com.luadministra.despacho.DespachoRepository;
import com.luadministra.despacho.EstadoDespacho;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.produccion.Produccion;
import com.luadministra.produccion.ProduccionRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.receta.Receta;
import com.luadministra.receta.RecetaDetalle;
import com.luadministra.receta.RecetaRepository;
import com.luadministra.rendicion.Rendicion;
import com.luadministra.rendicion.RendicionRepository;
import com.luadministra.venta.LineaVenta;
import com.luadministra.venta.Venta;
import com.luadministra.venta.VentaRepository;

import jakarta.transaction.Transactional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder {

    private final MateriaPrimaRepository materiaPrimaRepo;
    private final ProductoTerminadoRepository productoTerminadoRepo;
    private final RecetaRepository recetaRepo;
    private final CompraRepository compraRepo;
    private final ProduccionRepository produccionRepo;
    private final VentaRepository ventaRepo;
    private final CategoriaMateriaPrimaRepository categoriaMPRepo;
    private final CategoriaProductoTerminadoRepository categoriaPTRepo;
    private final ColaboradoraRepository colaboradoraRepo;
    private final DespachoRepository despachoRepo;
    private final RendicionRepository rendicionRepo;

    public DataSeeder(
            MateriaPrimaRepository materiaPrimaRepo,
            ProductoTerminadoRepository productoTerminadoRepo,
            RecetaRepository recetaRepo,
            CompraRepository compraRepo,
            ProduccionRepository produccionRepo,
            VentaRepository ventaRepo,
            CategoriaMateriaPrimaRepository categoriaMPRepo,
            CategoriaProductoTerminadoRepository categoriaPTRepo,
            ColaboradoraRepository colaboradoraRepo,
            DespachoRepository despachoRepo,
            RendicionRepository rendicionRepo) {
        this.materiaPrimaRepo = materiaPrimaRepo;
        this.productoTerminadoRepo = productoTerminadoRepo;
        this.recetaRepo = recetaRepo;
        this.compraRepo = compraRepo;
        this.produccionRepo = produccionRepo;
        this.ventaRepo = ventaRepo;
        this.categoriaMPRepo = categoriaMPRepo;
        this.categoriaPTRepo = categoriaPTRepo;
        this.colaboradoraRepo = colaboradoraRepo;
        this.despachoRepo = despachoRepo;
        this.rendicionRepo = rendicionRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (materiaPrimaRepo.count() > 0) return;

        var aceites = catMP("Aceites y Grasas", null);
        var esenciales = catMP("Aceites Esenciales", aceites);
        var polvos = catMP("Polvos y Arcillas", null);
        var ceras = catMP("Ceras", null);
        var harinas = catMP("Harinas y Almidones", polvos);

        var shampooCat = catPT("Shampoos", null);
        var cuidadoBucal = catPT("Cuidado Bucal", null);
        var desodorantesCat = catPT("Desodorantes", null);
        var rostro = catPT("Rostro y Cuidado Facial", null);

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
        comprar(karite, LocalDate.of(2026, 2, 10), 1000.0, 2500.0, "Mercado Central", null, "MLA123456789");
        comprar(coco, LocalDate.of(2025, 2, 1), 500.0, 800.0, "El Herbolario", null, null);
        comprar(coco, LocalDate.of(2025, 8, 15), 500.0, 950.0, "El Herbolario", null, null);
        comprar(coco, LocalDate.of(2026, 1, 20), 500.0, 1100.0, "El Herbolario", null, "MLA987654321");
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

        producir(shampooNormal, LocalDate.of(2025, 3, 1), 10.0);
        producir(shampooGraso, LocalDate.of(2025, 3, 1), 5.0);
        producir(pastaDental, LocalDate.of(2025, 3, 5), 8.0);
        producir(desodorante, LocalDate.of(2025, 3, 5), 5.0);
        producir(jabonFacial, LocalDate.of(2025, 3, 10), 12.0);
        producir(shampooNormal, LocalDate.of(2025, 6, 1), 8.0);
        producir(desodorante, LocalDate.of(2025, 7, 1), 6.0);
        producir(shampooGraso, LocalDate.of(2025, 9, 15), 5.0);
        producir(pastaDental, LocalDate.of(2025, 11, 1), 10.0);
        producir(shampooSeco, LocalDate.of(2025, 8, 1), 6.0);
        producir(balsamoLabial, LocalDate.of(2025, 8, 15), 10.0);
        producir(shampooNormal, LocalDate.of(2026, 1, 10), 12.0);
        producir(jabonFacial, LocalDate.of(2026, 2, 15), 10.0);
        producir(desodorante, LocalDate.of(2026, 3, 20), 8.0);

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

        // Colaboradoras
        var maria = colab("María González", "maria@email.com");
        var laura = colab("Laura Rodríguez", null);
        var ana = colab("Ana Martínez", "11-5555-1234");

        // Despachos (no descuentan stock, solo registran)
        var despacho1 = despacho(maria, LocalDate.of(2026, 3, 1),
                new DespachoLinea(shampooNormal, 10.0));
        var despacho2 = despacho(laura, LocalDate.of(2026, 3, 10),
                new DespachoLinea(pastaDental, 15.0));
        var despacho3 = despacho(ana, LocalDate.of(2026, 3, 15),
                new DespachoLinea(desodorante, 8.0));
        var despacho4 = despacho(maria, LocalDate.of(2026, 4, 1),
                new DespachoLinea(shampooGraso, 5.0));

        // Rendiciones sobre despacho1 (parcial + final)
        rendir(despacho1, 5100.0, LocalDate.of(2026, 3, 20),
                new RendicionLinea(shampooNormal, 6.0, 3.0));
        rendir(despacho1, 850.0, LocalDate.of(2026, 4, 5),
                new RendicionLinea(shampooNormal, 1.0, 0.0));

        // Rendición sobre despacho2 (parcial)
        rendir(despacho2, 6500.0, LocalDate.of(2026, 4, 10),
                new RendicionLinea(pastaDental, 10.0, 3.0));
    }

    private record VentaItem(ProductoTerminado pt, double cantidad) {}
    private record DespachoLinea(ProductoTerminado pt, double cantidad) {}
    private record RendicionLinea(ProductoTerminado pt, double vendida, double devuelta) {}

    private VentaItem item(ProductoTerminado pt, double cantidad) {
        return new VentaItem(pt, cantidad);
    }

    private CategoriaMateriaPrima catMP(String nombre, CategoriaMateriaPrima padre) {
        var c = new CategoriaMateriaPrima();
        c.setNombre(nombre);
        c.setCategoriaPadre(padre);
        return categoriaMPRepo.save(c);
    }

    private CategoriaProductoTerminado catPT(String nombre, CategoriaProductoTerminado padre) {
        var c = new CategoriaProductoTerminado();
        c.setNombre(nombre);
        c.setCategoriaPadre(padre);
        return categoriaPTRepo.save(c);
    }

    private MateriaPrima mp(String nombre, String unidad, double stock, Double stockMinimo, CategoriaMateriaPrima categoria) {
        var m = new MateriaPrima();
        m.setNombre(nombre);
        m.setUnidadMedida(unidad);
        m.setStockActual(stock);
        m.setStockMinimo(stockMinimo);
        m.setCategoria(categoria);
        return materiaPrimaRepo.save(m);
    }

    private ProductoTerminado pt(String nombre, double precio, double stock, Double stockMinimo, CategoriaProductoTerminado categoria) {
        var p = new ProductoTerminado();
        p.setNombre(nombre);
        p.setPrecioVenta(precio);
        p.setStockActual(stock);
        p.setStockMinimo(stockMinimo);
        p.setCategoria(categoria);
        return productoTerminadoRepo.save(p);
    }

    private RecetaDetalle det(MateriaPrima mp, double cantidad) {
        var d = new RecetaDetalle();
        d.setMateriaPrima(mp);
        d.setCantidad(cantidad);
        return d;
    }

    private void receta(ProductoTerminado pt, String notas, RecetaDetalle... detalles) {
        var r = new Receta();
        r.setProductoTerminado(pt);
        r.setNotas(notas);
        var lista = List.of(detalles);
        lista.forEach(d -> d.setReceta(r));
        r.setDetalles(lista);
        recetaRepo.save(r);
    }

    private void comprar(MateriaPrima mp, LocalDate fecha, double cantidad, double precio, String lugar, String url, String mlId) {
        var c = new Compra();
        c.setMateriaPrima(mp);
        c.setFecha(fecha);
        c.setCantidad(cantidad);
        c.setPrecio(precio);
        c.setLugar(lugar);
        c.setUrl(url);
        c.setMlId(mlId);
        compraRepo.save(c);
    }

    private void producir(ProductoTerminado pt, LocalDate fecha, double cantidad) {
        var p = new Produccion();
        p.setProductoTerminado(pt);
        p.setFecha(fecha);
        p.setCantidadFabricada(cantidad);
        produccionRepo.save(p);
    }

    private Colaboradora colab(String nombre, String contacto) {
        var c = new Colaboradora();
        c.setNombre(nombre);
        c.setContacto(contacto);
        return colaboradoraRepo.save(c);
    }

    private Despacho despacho(Colaboradora colab, LocalDate fecha, DespachoLinea... lineas) {
        var d = new Despacho();
        d.setColaboradora(colab);
        d.setFecha(fecha);
        for (var l : lineas) {
            var ld = new com.luadministra.despacho.LineaDespacho();
            ld.setDespacho(d);
            ld.setProductoTerminado(l.pt());
            ld.setCantidad(l.cantidad());
            ld.setPrecioUnitario(l.pt().getPrecioVenta());
            d.getLineas().add(ld);
        }
        return despachoRepo.save(d);
    }

    private void rendir(Despacho despacho, double monto, LocalDate fecha, RendicionLinea... lineas) {
        var r = new Rendicion();
        r.setDespacho(despacho);
        r.setMontoEntregado(monto);
        r.setFecha(fecha);
        for (var l : lineas) {
            var lr = new com.luadministra.rendicion.LineaRendicion();
            lr.setRendicion(r);
            lr.setProductoTerminado(l.pt());
            lr.setCantidadVendida(l.vendida());
            lr.setCantidadDevuelta(l.devuelta());
            r.getLineas().add(lr);

            if (l.vendida() > 0) {
                l.pt().setStockActual(l.pt().getStockActual() - l.vendida());
            }
            if (l.devuelta() > 0) {
                l.pt().setStockActual(l.pt().getStockActual() + l.devuelta());
            }
            productoTerminadoRepo.save(l.pt());

            if (l.vendida() > 0) {
                var venta = new Venta();
                venta.setFecha(fecha);
                var vl = new LineaVenta();
                vl.setVenta(venta);
                vl.setProductoTerminado(l.pt());
                vl.setCantidad(l.vendida());
                vl.setPrecioUnitario(l.pt().getPrecioVenta());
                venta.setLineas(List.of(vl));
                ventaRepo.save(venta);
            }
        }
        rendicionRepo.save(r);

        double totalDespachado = despacho.getLineas().stream()
                .mapToDouble(ld -> ld.getCantidad())
                .sum();
        var anteriores = rendicionRepo.findByDespachoId(despacho.getId());
        double totalRendido = anteriores.stream()
                .flatMap(rr -> rr.getLineas().stream())
                .mapToDouble(lr -> lr.getCantidadVendida() + lr.getCantidadDevuelta())
                .sum();
        if (totalRendido >= totalDespachado) {
            despacho.setEstado(EstadoDespacho.RENDIDO_TOTAL);
        } else {
            despacho.setEstado(EstadoDespacho.RENDIDO_PARCIAL);
        }
        despachoRepo.save(despacho);
    }

    private void vender(LocalDate fecha, VentaItem... items) {
        var v = new Venta();
        v.setFecha(fecha);
        var lineas = new ArrayList<LineaVenta>();
        for (var i : items) {
            var l = new LineaVenta();
            l.setVenta(v);
            l.setProductoTerminado(i.pt());
            l.setCantidad(i.cantidad());
            l.setPrecioUnitario(i.pt().getPrecioVenta());
            lineas.add(l);
        }
        v.setLineas(lineas);
        ventaRepo.save(v);
    }
}
