package org.example;

import java.sql.*;
import java.util.Scanner;
public class CiclistaNacionalidadAmpliado {
    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(
                DBConfig.getUrl(),
                DBConfig.getUser(),
                DBConfig.getPassword());
            Statement stmt = conn.createStatement()) {
            Scanner sc = new Scanner(System.in);
            boolean continuar = true;

            //avisamos que la conexion ha sido exitosa
            System.out.println("Conectado a la base de datos");

            //mostramos todos los ciclistas
            mostrar(stmt);

            while(continuar){
                //abrimos el menu y dejamos al usuario elegir
                System.out.println("Elige la opcion (0=apagar, 1=buscar por pais, 2=insertar, 3=modificar, 4=eliminar, 5=listar):");
                int opcion = sc.nextInt();
                switch (opcion){
                    case 0:
                        continuar = false;
                        System.out.println("Saliendo...");
                        break;
                    case 1:
                        sc.nextLine();
                        System.out.println("Introduce un pais:");
                        String pais = sc.nextLine();

                        mostrar(stmt, pais);
                        break;
                    case 2:
                        insertar(stmt, conn);
                        break;
                    case 3:
                        modificar(conn);
                        break;
                    case 4:
                        eliminar(conn);
                        break;
                    case 5:
                        mostrar(stmt);
                        break;
                }
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    //con el metodo mostrar realizamos una sobrecarga para poder usarlo tambien para buscar por nacionalidad
    public static void mostrar(Statement stmt) throws SQLException {
        String sql = "SELECT id_ciclista, ciclista.nombre as nombre, nacionalidad, edad, equipo.nombre as equipo FROM ciclista JOIN equipo USING (id_equipo) ORDER BY id_ciclista ASC";
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
            int id_ciclista = rs.getInt("id_ciclista");
            String nombre = rs.getString("nombre");
            String nacionalidad = rs.getString("nacionalidad");
            int edad = rs.getInt("edad");
            String id_equipo = rs.getString("equipo");

            System.out.println(id_ciclista + "-" + nombre + "-" + nacionalidad + "-" + edad + "-" + id_equipo);
        }
    }

    public static void mostrar(Statement stmt, String pais) throws SQLException {
        String sql = "SELECT id_ciclista, ciclista.nombre as nombre, nacionalidad, edad, equipo.nombre as equipo FROM ciclista JOIN equipo USING (id_equipo) WHERE nacionalidad = '" + pais + "' ORDER BY id_ciclista ASC";
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
            int id_ciclista = rs.getInt("id_ciclista");
            String nombre = rs.getString("nombre");
            String nacionalidad = rs.getString("nacionalidad");
            int edad = rs.getInt("edad");
            String id_equipo = rs.getString("equipo");

            System.out.println(id_ciclista + "-" + nombre + "-" + nacionalidad + "-" + edad + "-" + id_equipo);
        }
    }

    public static void insertar(Statement stmt, Connection conn) throws SQLException {
        Scanner sc = new Scanner(System.in);
        PreparedStatement ps;
        int id = 0;

        //buscamos el id_ciclista maximo y le sumamos 1
        String idMax = "SELECT MAX(id_ciclista) as maximo FROM ciclista";
        ResultSet rs = stmt.executeQuery(idMax);
        while(rs.next()){
            id = rs.getInt("maximo");
            id++;
        }

        //pedimos nombre nacionalidad y edad
        System.out.println("Introduce el nombre del ciclista:");
        String nombre = sc.nextLine();

        System.out.println("Introduce la nacionalidad del ciclista:");
        String nacionalidad = sc.nextLine();

        System.out.println("Introduce la edad del ciclista: (>18)");
        int edad = sc.nextInt();
        //limpiamos buffer
        sc.nextLine();

        //pedimos el id_equipo y comprobamos si existe
        System.out.println("Introduce el id de equipo del ciclista:");
        int id_equipo = sc.nextInt();

        if(existe(conn, id_equipo, "equipo")){
            //en caso de que si insertamos al ciclista
            String sql = "INSERT INTO ciclista VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setString(2, nombre);
            ps.setString(3, nacionalidad);
            ps.setInt(4, edad);
            ps.setInt(5, id_equipo);
            ps.executeUpdate();
            System.out.println("Ciclista insertado.");
        }else{
            System.out.println("No existe el id de equipo.");
        }
    }

    public static void modificar(Connection conn) throws SQLException {
        Scanner sc = new Scanner(System.in);
        PreparedStatement ps;

        //pedimos el id y comprobamos que exista
        System.out.println("Introduce el id del ciclista:");
        int id = sc.nextInt();

        if(existe(conn, id, "ciclista")){
            System.out.println("Introduce la nueva edad del ciclista:");
            int edad = sc.nextInt();
            //limpimos buffer
            sc.nextLine();

            //pedimos el nuevo id del equipo y comprobamos que exista
            System.out.println("Introduce el nuevo id del equipo del ciclista:");
            int id_equipo = sc.nextInt();

            if(existe(conn, id_equipo, "equipo")){
                //si existe hacemos el update
                String modificar = "UPDATE ciclista SET edad = ?, id_equipo = ? WHERE id_ciclista = ?";
                ps = conn.prepareStatement(modificar);
                ps.setInt(1, edad);
                ps.setInt(2, id_equipo);
                ps.setInt(3, id);
                ps.executeUpdate();
                System.out.println("Ciclista modificado.");
            }else{
                System.out.println("No existe el id del equipo.");
            }
        }
    }

    public static void eliminar(Connection conn) throws SQLException {
        Scanner sc = new Scanner(System.in);
        PreparedStatement ps;

        //pedimos el id y comprobamos qu exista
        System.out.println("Introduce el id del ciclista:");
        int id = sc.nextInt();
        if(existe(conn, id, "ciclista")){
            //primero borramos la participacion del ciclista
            String sql = "DELETE FROM participacion WHERE id_ciclista = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            //y luego lo eliminamos a el
            sql = "DELETE FROM ciclista WHERE id_ciclista = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Ciclista eliminado.");
        }
    }

    public static boolean existe(Connection conn, int id, String donde) throws SQLException {
        PreparedStatement ps;
        String existe = "";
        ResultSet rs;
        existe = switch (donde) {
            //si le pasamos la palabra "equipo", comparamos el id con id_equipo
            case "equipo" -> "SELECT * FROM ciclista WHERE id_equipo = ?";
            //si le pasamos la palabra "ciclista", comparamos el id con id_ciclista
            case "ciclista" -> "SELECT * FROM ciclista WHERE id_ciclista = ?";
            default -> existe;
        };
        ps = conn.prepareStatement(existe);
        ps.setInt(1, id);
        rs = ps.executeQuery();
        return rs.next();
    }
}