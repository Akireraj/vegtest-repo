package ar.edu.vetgest.dao.jdbc;

import ar.edu.vetgest.dao.CatalogoDAO;
import ar.edu.vetgest.modelo.Cliente;
import ar.edu.vetgest.modelo.Especie;
import ar.edu.vetgest.modelo.Mascota;
import ar.edu.vetgest.modelo.Rol;
import ar.edu.vetgest.modelo.Sucursal;
import ar.edu.vetgest.modelo.Usuario;
import ar.edu.vetgest.modelo.Veterinario;
import ar.edu.vetgest.util.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CatalogoDAOJdbc implements CatalogoDAO {

    private static final String SQL_MASCOTA = "SELECT id_mascota, id_cliente, id_especie, nombre, raza, sexo, "
            + "fecha_nac, activa FROM mascota";

    @Override
    public List<Sucursal> listarSucursales() {
        String sql = "SELECT id_sucursal, nombre, direccion, telefono FROM sucursal ORDER BY id_sucursal";
        List<Sucursal> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Sucursal(rs.getInt("id_sucursal"), rs.getString("nombre"),
                        rs.getString("direccion"), rs.getString("telefono")));
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("listar sucursales", e);
        }
        return lista;
    }

    @Override
    public List<Usuario> listarUsuariosActivos() {
        String sql = "SELECT id_usuario, id_sucursal, id_veterinario, username, rol FROM usuario "
                + "WHERE activo = TRUE ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idVet = rs.getInt("id_veterinario");
                Integer idVeterinario = rs.wasNull() ? null : idVet;
                lista.add(new Usuario(rs.getInt("id_usuario"), rs.getInt("id_sucursal"), idVeterinario,
                        rs.getString("username"), Rol.valueOf(rs.getString("rol"))));
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("listar usuarios", e);
        }
        return lista;
    }

    @Override
    public List<Veterinario> listarVeterinarios() {
        String sql = "SELECT * FROM veterinario ORDER BY apellido, nombre";
        List<Veterinario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearVeterinario(rs));
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("listar veterinarios", e);
        }
        return lista;
    }

    @Override
    public Optional<Veterinario> buscarVeterinario(int idVeterinario) {
        String sql = "SELECT * FROM veterinario WHERE id_veterinario = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVeterinario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearVeterinario(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("buscar veterinario", e);
        }
    }

    @Override
    public List<Cliente> listarClientesConMascotas() {
        Map<Integer, Cliente> clientes = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.obtener()) {
            try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM cliente ORDER BY id_cliente");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente(rs.getInt("id_cliente"), rs.getString("dni"), rs.getString("nombre"),
                            rs.getString("apellido"), rs.getString("telefono"), rs.getString("email"));
                    clientes.put(c.getId(), c);
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(SQL_MASCOTA + " WHERE activa = TRUE ORDER BY id_mascota");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mascota m = mapearMascota(rs);
                    Cliente duenio = clientes.get(m.getIdCliente());
                    if (duenio != null) {
                        duenio.agregarMascota(m);
                    }
                }
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("listar clientes", e);
        }
        return new ArrayList<>(clientes.values());
    }

    @Override
    public Optional<Mascota> buscarMascota(int idMascota) {
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_MASCOTA + " WHERE id_mascota = ?")) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearMascota(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Jdbc.traducir("buscar mascota", e);
        }
    }

    @Override
    public int insertarCliente(Cliente c) {
        String sql = "INSERT INTO cliente (dni, nombre, apellido, telefono, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getApellido());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.executeUpdate();
            int id = claveGenerada(ps);
            c.asignarId(id);
            return id;
        } catch (SQLException e) {
            throw Jdbc.traducir("registrar el cliente", e);
        }
    }

    @Override
    public int insertarMascota(Mascota m) {
        String sql = "INSERT INTO mascota (id_cliente, id_especie, nombre, raza, sexo, fecha_nac) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getIdCliente());
            ps.setInt(2, m.getEspecie().getId());
            ps.setString(3, m.getNombre());
            ps.setString(4, m.getRaza());
            ps.setString(5, String.valueOf(m.getSexo()));
            if (m.getFechaNacimiento() == null) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(m.getFechaNacimiento()));
            }
            ps.executeUpdate();
            int id = claveGenerada(ps);
            m.asignarId(id);
            return id;
        } catch (SQLException e) {
            throw Jdbc.traducir("registrar la mascota", e);
        }
    }

    static int claveGenerada(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) {
                throw new SQLException("La base no devolvio la clave generada");
            }
            return rs.getInt(1);
        }
    }

    private static Veterinario mapearVeterinario(ResultSet rs) throws SQLException {
        return new Veterinario(rs.getInt("id_veterinario"), rs.getInt("id_sucursal"), rs.getString("matricula"),
                rs.getString("nombre"), rs.getString("apellido"), rs.getString("especialidad"));
    }

    private static Mascota mapearMascota(ResultSet rs) throws SQLException {
        Date nacimiento = rs.getDate("fecha_nac");
        Mascota m = Mascota.crear(Especie.desdeId(rs.getInt("id_especie")), rs.getInt("id_mascota"),
                rs.getInt("id_cliente"), rs.getString("nombre"), rs.getString("raza"),
                rs.getString("sexo").charAt(0), nacimiento == null ? null : nacimiento.toLocalDate());
        if (!rs.getBoolean("activa")) {
            m.darDeBaja();
        }
        return m;
    }
}
