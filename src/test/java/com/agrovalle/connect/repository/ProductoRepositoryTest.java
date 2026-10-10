package com.agrovalle.connect.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Producto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductoRepositoryTest {

    @Autowired
    private AgricultorRepository agricultorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void debeFiltrarPorMunicipioDelAgricultorYCategoria() {
        Agricultor agricultorPalmira = crearAgricultor("1112067800", "Palmira");
        Agricultor agricultorCali = crearAgricultor("1114780500", "Cali");
        productoRepository.save(crearProducto("Aguacate", "Frutas", agricultorPalmira));
        productoRepository.save(crearProducto("Tomate", "Verduras", agricultorPalmira));
        productoRepository.save(crearProducto("Banano", "Frutas", agricultorCali));

        List<Producto> productos = productoRepository
                .findByAgricultorMunicipioAndCategoria("Palmira", "Frutas");

        assertEquals(List.of("Aguacate"), productos.stream().map(Producto::getNombre).toList());
    }

    @Test
    void debeDevolverListaVaciaCuandoNoHayCoincidencias() {
        Agricultor agricultor = crearAgricultor("1111078900", "Palmira");
        productoRepository.save(crearProducto("Aguacate", "Frutas", agricultor));

        List<Producto> productos = productoRepository
                .findByAgricultorMunicipioAndCategoria("Tuluá", "Frutas");

        assertEquals(List.of(), productos);
    }

    private Agricultor crearAgricultor(String identificacion, String municipio) {
        return agricultorRepository.save(new Agricultor(
                "Agricultor",
                "De Prueba",
                identificacion,
                municipio,
                "3001234567",
                identificacion + "@correo.com",
                "contrasena123"));
    }

    private Producto crearProducto(String nombre, String categoria, Agricultor agricultor) {
        return new Producto(
                nombre,
                categoria,
                10,
                new BigDecimal("3500"),
                LocalDate.now().plusDays(5),
                agricultor);
    }
}
