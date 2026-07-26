package com.luadministra.consignatario;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.jdt.annotation.Nullable;

@Entity
@Table(name = "consignatario")
public class Consignatario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    private @Nullable String contacto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public @Nullable String getContacto() { return contacto; }
    public void setContacto(@Nullable String contacto) { this.contacto = contacto; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consignatario other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
