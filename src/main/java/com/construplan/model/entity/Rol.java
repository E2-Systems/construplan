package com.construplan.model.entity;

import java.util.Arrays;
import java.util.Optional;

public enum Rol {

	ADMIN("Administrador", "/admin/dashboard", "ROLE_ADMIN"),
	OFICINA("Ingeniero de Oficina", "/oficina/dashboard", "ROLE_OFICINA"),
	CAMPO("Ingeniero de Campo", "/campo/dashboard", "ROLE_CAMPO"),
	EMPLEADO("Empleado", "/empleado/dashboard", "ROLE_EMPLEADO");

	private final String descripcion;
	private final String dashboardUrl;
	private final String authority;

	Rol(String descripcion, String dashboardUrl, String authority) {
		this.descripcion = descripcion;
		this.dashboardUrl = dashboardUrl;
		this.authority = authority;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public String getDashboardUrl() {
		return dashboardUrl;
	}

	public String getAuthority() {
		return authority;
	}

	public static Optional<Rol> fromString(String rol) {
		if (rol == null)
			return Optional.empty();
		try {
			return Optional.of(Rol.valueOf(rol.trim().toUpperCase()));
		    } catch (IllegalArgumentException ex) {
		        return Optional.empty();
		    }
		}

		// esValido se simplifica
		public static boolean esValido(String rol) {
		    return fromString(rol).isPresent();
		}

		// Útil para buscar por authority ("ROLE_ADMIN" → ADMIN)
		public static Optional<Rol> fromAuthority(String authority) {
			return Arrays.stream(values()).filter(r -> r.authority.equals(authority)).findFirst();
		}
}
