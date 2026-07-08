package com.luadministra.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {"/", "/dashboard", "/materias-primas", "/compras",
                             "/productos-terminados", "/recetas", "/produccion", "/ventas"})
    public String forward() {
        return "forward:/index.html";
    }
}
