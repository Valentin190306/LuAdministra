package com.luadministra.config;

import com.luadministra.categoriamateriaprima.CategoriaMateriaPrima;
import com.luadministra.categoriamateriaprima.CategoriaMateriaPrimaRepository;
import com.luadministra.categoriaproductoterminado.CategoriaProductoTerminado;
import com.luadministra.categoriaproductoterminado.CategoriaProductoTerminadoRepository;
import com.luadministra.compra.Compra;
import com.luadministra.compra.CompraRepository;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.produccion.Produccion;
import com.luadministra.produccion.ProduccionRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.receta.Receta;
import com.luadministra.receta.RecetaDetalle;
import com.luadministra.receta.RecetaRepository;
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

    public DataSeeder(
            MateriaPrimaRepository materiaPrimaRepo,
            ProductoTerminadoRepository productoTerminadoRepo,
            RecetaRepository recetaRepo,
            CompraRepository compraRepo,
            ProduccionRepository produccionRepo,
            VentaRepository ventaRepo,
            CategoriaMateriaPrimaRepository categoriaMPRepo,
            CategoriaProductoTerminadoRepository categoriaPTRepo) {
        this.materiaPrimaRepo = materiaPrimaRepo;
        this.productoTerminadoRepo = productoTerminadoRepo;
        this.recetaRepo = recetaRepo;
        this.compraRepo = compraRepo;
        this.produccionRepo = produccionRepo;
        this.ventaRepo = ventaRepo;
        this.categoriaMPRepo = categoriaMPRepo;
        this.categoriaPTRepo = categoriaPTRepo;
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

        comprar(karite, LocalDate.of(2024, 7, 10), 1000.0, 1400.0, "Mercado Central", null);
        comprar(karite, LocalDate.of(2025, 1, 15), 1000.0, 1500.0, "Mercado Central", "https://mercadocentral.example.com/manteca-karite");
        comprar(karite, LocalDate.of(2025, 3, 20), 1000.0, 1800.0, "Mercado Central", null);
        comprar(karite, LocalDate.of(2025, 9, 5), 1000.0, 2200.0, "Distribuidora Química", "https://distribuidora-quimica.example.com/producto/123");
        comprar(karite, LocalDate.of(2026, 2, 10), 1000.0, 2500.0, "Mercado Central", null);
        comprar(coco, LocalDate.of(2025, 2, 1), 500.0, 800.0, "El Herbolario", null);
        comprar(coco, LocalDate.of(2025, 8, 15), 500.0, 950.0, "El Herbolario", null);
        comprar(coco, LocalDate.of(2026, 1, 20), 500.0, 1100.0, "El Herbolario", null);
        comprar(bicarbonato, LocalDate.of(2025, 1, 10), 2000.0, 400.0, "Distribuidora Química", null);
        comprar(bicarbonato, LocalDate.of(2025, 11, 20), 2000.0, 450.0, "Distribuidora Química", null);
        comprar(menta, LocalDate.of(2025, 2, 15), 100.0, 2500.0, "Aromas Naturales", null);
        comprar(menta, LocalDate.of(2025, 7, 1), 100.0, 2750.0, "Aromas Naturales", null);
        comprar(arcilla, LocalDate.of(2025, 3, 1), 1000.0, 600.0, "El Herbolario", null);
        comprar(arcilla, LocalDate.of(2025, 10, 10), 1000.0, 650.0, "El Herbolario", null);
        comprar(carnauba, LocalDate.of(2025, 4, 15), 500.0, 1800.0, "Distribuidora Química", "https://distribuidora-quimica.example.com/cera-carnauba");
        comprar(oliva, LocalDate.of(2025, 5, 10), 500.0, 1200.0, null, null);
        comprar(almendras, LocalDate.of(2025, 6, 1), 300.0, 1500.0, "El Herbolario", null);
        comprar(lavanda, LocalDate.of(2025, 6, 15), 80.0, 3200.0, "Aromas Naturales", "https://aromas-naturales.example.com/lavanda");
        comprar(mantecaCacao, LocalDate.of(2025, 7, 20), 500.0, 1600.0, "Mercado Central", null);
        comprar(caliza, LocalDate.of(2025, 8, 1), 800.0, 350.0, "Distribuidora Química", null);
        comprar(mantecaCacao, LocalDate.of(2026, 3, 5), 500.0, 1900.0, "El Herbolario", null);
        comprar(lavanda, LocalDate.of(2026, 4, 1), 100.0, 3400.0, "Aromas Naturales", "https://aromas-naturales.example.com/lavanda");

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
    }

    private record VentaItem(ProductoTerminado pt, double cantidad) {}

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

    private void comprar(MateriaPrima mp, LocalDate fecha, double cantidad, double precio, String lugar, String url) {
        var c = new Compra();
        c.setMateriaPrima(mp);
        c.setFecha(fecha);
        c.setCantidad(cantidad);
        c.setPrecio(precio);
        c.setLugar(lugar);
        c.setUrl(url);
        compraRepo.save(c);
    }

    private void producir(ProductoTerminado pt, LocalDate fecha, double cantidad) {
        var p = new Produccion();
        p.setProductoTerminado(pt);
        p.setFecha(fecha);
        p.setCantidadFabricada(cantidad);
        produccionRepo.save(p);
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
