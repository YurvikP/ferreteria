/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Ferreteria.controller;

import com.Ferreteria.dao.UsuarioDao;
import com.Ferreteria.domain.Carrito;
import com.Ferreteria.domain.CarritoDetalle;
import com.Ferreteria.domain.Usuario;
import com.Ferreteria.service.ArticuloService;
import com.Ferreteria.service.CarritoDetalleService;
import com.Ferreteria.service.CarritoService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@Slf4j
public class IndexController {
    @Autowired
    private ArticuloService articuloService;
    @Autowired
    private CarritoService carritoService;
    @Autowired
    private CarritoDetalleService carritoDetalleService;
    @Autowired
    private UsuarioDao usuarioDao;
    @GetMapping("/")
    public String inicio(Model model, HttpServletRequest request) {        
        //Obtener el usuario llegado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails user = null;
        if (principal instanceof UserDetails) {
            user = (UserDetails) principal;
        }
        //Validar si es usuario de un cliente
        boolean esCliente = false;
        if (user.getAuthorities().size() == 1) {
            for (var rol : user.getAuthorities()) {
                if (rol.getAuthority().equals("ROLE_USER")) {
                    esCliente = true;
                }
            }
        }
        if (esCliente) {
            Usuario usuario = usuarioDao.findByUsername(user.getUsername());
            Carrito carrito = carritoService.getCarritoCliente(usuario.getIdCliente());
            request.getSession().setAttribute("idCliente", usuario.getIdCliente());
            request.getSession().setAttribute("idCarrito", carrito.getIdCarrito());
            //Consultar los items
            List<CarritoDetalle> carritoDetalles = carritoDetalleService.getCarritoDetalles(carrito.getIdCarrito());
            int cantidadArticulosCarrito = carritoDetalles.size();
            model.addAttribute("cantidadArticulosCarrito", cantidadArticulosCarrito);
        }
        log.info("Ahora se usa arquitectura MVC");
        var articulos = articuloService.getArticulos(true);
        model.addAttribute("articulos", articulos);
        model.addAttribute("esCliente", esCliente);
        return "index";
    }
}