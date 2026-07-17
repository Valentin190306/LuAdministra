package com.luadministra.config;

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
import com.luadministra.venta.Venta;
import com.luadministra.venta.VentaRepository;

import jakarta.transaction.Transactional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder {

    private final MateriaPrimaRepository materiaPrimaRepo;
    private final ProductoTerminadoRepository productoTerminadoRepo;
    private final RecetaRepository recetaRepo;
    private final CompraRepository compraRepo;
    private final ProduccionRepository produccionRepo;
    private final VentaRepository ventaRepo;

    public DataSeeder(
            MateriaPrimaRepository materiaPrimaRepo,
            ProductoTerminadoRepository productoTerminadoRepo,
            RecetaRepository recetaRepo,
            CompraRepository compraRepo,
            ProduccionRepository produccionRepo,
            VentaRepository ventaRepo) {
        this.materiaPrimaRepo = materiaPrimaRepo;
        this.productoTerminadoRepo = productoTerminadoRepo;
        this.recetaRepo = recetaRepo;
        this.compraRepo = compraRepo;
        this.produccionRepo = produccionRepo;
        this.ventaRepo = ventaRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (materiaPrimaRepo.count() > 0)
            return;

        var karite = mp("Manteca de Karité", "g", 500.0, 200.0);
        var coco = mp("Aceite de Coco", "ml", 300.0, 150.0);
        var bicarbonato = mp("Bicarbonato de Sodio", "g", 1000.0, 500.0);
        var arcilla = mp("Arcilla Blanca", "g", 800.0, 300.0);
        var menta = mp("Aceite Esencial de Menta", "ml", 50.0, 20.0);
        var carnauba = mp("Cera de Carnaúba", "g", 200.0, 100.0);
        var maiz = mp("Almidón de Maíz", "g", 1500.0, 500.0);
        var oliva = mp("Aceite de Oliva", "ml", 200.0, 100.0);
        var almendras = mp("Aceite de Almendras", "ml", 150.0, 75.0);

        var shampooNormal = pt("Shampoo en Barra - Cabello Normal", 850.0, 8.0, 5.0);
        var shampooGraso = pt("Shampoo en Barra - Cabello Graso", 850.0, 3.0, 5.0);
        var pastaDental = pt("Pasta Dental Natural", 650.0, 7.0, 5.0);
        var desodorante = pt("Desodorante en Barra", 600.0, 2.0, 5.0);
        var jabonFacial = pt("Jabón Facial Natural", 450.0, 12.0, 5.0);

        receta(shampooNormal,
                det(karite, 50.0),
                det(coco, 30.0),
                det(menta, 2.0),
                det(carnauba, 10.0));

        receta(shampooGraso,
                det(arcilla, 40.0),
                det(coco, 20.0),
                det(menta, 3.0),
                det(maiz, 10.0));

        receta(pastaDental,
                det(bicarbonato, 30.0),
                det(coco, 15.0),
                det(menta, 1.0));

        receta(desodorante,
                det(karite, 25.0),
                det(bicarbonato, 15.0),
                det(maiz, 10.0),
                det(menta, 1.0));

        receta(jabonFacial,
                det(arcilla, 20.0),
                det(oliva, 30.0),
                det(almendras, 10.0));

        comprar(karite, LocalDate.of(2025, 1, 15), 1000.0, 1500.0, "Mercado Central");
        comprar(karite, LocalDate.of(2025, 3, 20), 1000.0, 1800.0, "Mercado Central");
        comprar(coco, LocalDate.of(2025, 2, 1), 500.0, 800.0, "El Herbolario");
        comprar(bicarbonato, LocalDate.of(2025, 1, 10), 2000.0, 400.0, "Distribuidora Química");
        comprar(menta, LocalDate.of(2025, 2, 15), 100.0, 2500.0, "Aromas Naturales");
        comprar(arcilla, LocalDate.of(2025, 3, 1), 1000.0, 600.0, "El Herbolario");

        producir(shampooNormal, LocalDate.of(2025, 3, 1), 10.0);
        producir(shampooGraso, LocalDate.of(2025, 3, 1), 5.0);
        producir(pastaDental, LocalDate.of(2025, 3, 5), 8.0);
        producir(desodorante, LocalDate.of(2025, 3, 5), 5.0);
        producir(jabonFacial, LocalDate.of(2025, 3, 10), 12.0);

        vender(shampooNormal, LocalDate.of(2025, 3, 15), 2.0);
        vender(desodorante, LocalDate.of(2025, 3, 15), 3.0);
        vender(shampooGraso, LocalDate.of(2025, 3, 20), 2.0);
        vender(pastaDental, LocalDate.of(2025, 3, 22), 1.0);
    }

    private MateriaPrima mp(String nombre, String unidad, double stock, Double stockMinimo) {
        var m = new MateriaPrima();
        m.setNombre(nombre);
        m.setUnidadMedida(unidad);
        m.setStockActual(stock);
        m.setStockMinimo(stockMinimo);
        return materiaPrimaRepo.save(m);
    }

    private ProductoTerminado pt(String nombre, double precio, double stock, Double stockMinimo) {
        var p = new ProductoTerminado();
        p.setNombre(nombre);
        p.setPrecioVenta(precio);
        p.setStockActual(stock);
        p.setStockMinimo(stockMinimo);
        return productoTerminadoRepo.save(p);
    }

    private RecetaDetalle det(MateriaPrima mp, double cantidad) {
        var d = new RecetaDetalle();
        d.setMateriaPrima(mp);
        d.setCantidad(cantidad);
        return d;
    }

    private void receta(ProductoTerminado pt, RecetaDetalle... detalles) {
        var r = new Receta();
        r.setProductoTerminado(pt);
        var lista = List.of(detalles);
        lista.forEach(d -> d.setReceta(r));
        r.setDetalles(lista);
        recetaRepo.save(r);
    }

    private void comprar(MateriaPrima mp, LocalDate fecha, double cantidad, double precio, String lugar) {
        var c = new Compra();
        c.setMateriaPrima(mp);
        c.setFecha(fecha);
        c.setCantidad(cantidad);
        c.setPrecio(precio);
        c.setLugar(lugar);
        compraRepo.save(c);
    }

    private void producir(ProductoTerminado pt, LocalDate fecha, double cantidad) {
        var p = new Produccion();
        p.setProductoTerminado(pt);
        p.setFecha(fecha);
        p.setCantidadFabricada(cantidad);
        produccionRepo.save(p);
    }

    private void vender(ProductoTerminado pt, LocalDate fecha, double cantidad) {
        var v = new Venta();
        v.setProductoTerminado(pt);
        v.setFecha(fecha);
        v.setCantidad(cantidad);
        ventaRepo.save(v);
    }
}
