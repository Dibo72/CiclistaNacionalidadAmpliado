package org.example;

import java.sql.*;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class CiclistaNacionalidad {
    public static void main(String[] args) {
        try(Connection conn = DriverManager.getConnection(
                DBConfig.getUrl(),
                DBConfig.getUser(),
                DBConfig.getPassword());
            Statement stmt = conn.createStatement()) {
            Scanner sc = new Scanner(System.in);

            System.out.println("Conectado a la base de datos");

            mostrar(stmt);

            System.out.println("Introduce un pais:");
            String pais = sc.nextLine();

            mostrar(stmt, pais);

        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void mostrar(Statement stmt) throws SQLException {
        String sql = "SELECT id_ciclista, ciclista.nombre as nombre, nacionalidad, edad, equipo.nombre as equipo FROM ciclista JOIN equipo USING (id_equipo)";
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
        String sql = "SELECT id_ciclista, ciclista.nombre as nombre, nacionalidad, edad, equipo.nombre as equipo FROM ciclista JOIN equipo USING (id_equipo) WHERE nacionalidad = '" + pais + "'";
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
}