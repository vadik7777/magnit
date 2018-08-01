/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.magnit.summa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author KonovalovVA
 */
class TestThread implements Runnable {

    private DBPool pool;
    private int begin;
    private int end;
    private String query;

    public TestThread(int begin, int end, DBPool pool, String query) {
        this.begin = begin;
        this.end = end;
        this.pool = pool;
        this.query = query;
    }

    public TestThread(DBPool pool, String query) {
        this.pool = pool;
        this.query = query;
    }

    public TestThread(String query) {
        this.query = query;
    }

    @Override
    public void run() {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            if (query.equals("insert")) {
                System.out.println(Thread.currentThread().toString() + " start insert thread ");
                con = pool.getConnection();// получем соединение к БД
                st = con.createStatement();
                for (int i = begin; i <= end; i++) {
                    st.executeUpdate("insert into TEST (FIELD) values('" + i + "')");
                }
                st.close();
                pool.putConnection(con);
                System.out.println(Thread.currentThread().toString() + " end insert thread");
            } else if (query.equals("delete")) {
                System.out.println(Thread.currentThread().toString() + " start delete thread");
                con = pool.getConnection();// получем соединение к БД
                st = con.createStatement();
                st.executeUpdate("DELETE FROM TEST");
                st.close();
                pool.putConnection(con);
                System.out.println(Thread.currentThread().toString() + " end delete thread");
            } else if (query.equals("xml")) {
                System.out.println(Thread.currentThread().toString() + " start 1.xml thread");
                con = pool.getConnection();// получем соединение к БД
                st = con.createStatement();
                rs = st.executeQuery("select FIELD from TEST order by FIELD asc");
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("1.xml"));
                    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    writer.newLine();
                    writer.write("<entries>");
                    writer.newLine();
                    while (rs.next()) {
                        writer.write("<entry>");
                        writer.newLine();
                        writer.write("<field>" + rs.getInt(1) + "</field>");
                        writer.newLine();
                        writer.write("</entry>");
                        writer.newLine();
                    }
                    writer.write("</entries>");
                    writer.newLine();
                    writer.flush();
                    writer.close();

                } catch (IOException ex) {
                    System.out.println("Проблемы записи в файл 1.xml");
                    ex.printStackTrace();
                }
                rs.close();
                st.close();
                pool.putConnection(con);
                System.out.println(Thread.currentThread().toString() + " end 1.xml thread");
            }

        } catch (SQLException ex) {
            //ошибка при выполнении, выводим в консоль
            System.out.println("Ошибка базы данных");
            ex.printStackTrace();
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                //ignore
            }
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                //ignore
            }
            try {
                if (con != null) {
                    pool.putConnection(con); // кладем соединение обратно в пул
                }
            } catch (SQLException e) {
                //ignore
            }
        }
        if (query.equals("xsl")) {
            System.out.println(Thread.currentThread().toString() + " start 2.xml thread");
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new File("2.xsl"));
            Transformer transformer = null;
            try {
                transformer = factory.newTransformer(xslt);
            } catch (TransformerConfigurationException ex) {
                System.out.println("Проблемы файла конфигурации XSL");
                ex.printStackTrace();
            }
            Source xml = new StreamSource(new File("1.xml"));
            try {
                transformer.transform(xml, new StreamResult(new File("2.xml")));
            } catch (TransformerException ex) {
                System.out.println("Проблемы записи в файл 2.xml");
                ex.printStackTrace();
            }
            System.out.println(Thread.currentThread().toString() + " end 2.xml thread");
        } else if (query.equals("sum")) {
            System.out.println(Thread.currentThread().toString() + " start summa thread");
            BufferedReader br;
            String line;
            long sum = 0;
            try {
                br = new BufferedReader(new FileReader("2.xml"));    
                while ((line = br.readLine()) != null) {
                    if (line.contains("field")) {
                        try {
                            sum += Integer.parseInt(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } catch (NumberFormatException ex) {
                            System.out.println("Проблемы с данными файла 2.xml");
                            ex.printStackTrace();
                        }
                    }
                }
                br.close();
            } catch (IOException ex) {
                System.out.println("Проблемы с файлом 2.xml");
                ex.printStackTrace();
            }
            System.out.println("summa = " + sum);
            System.out.println(Thread.currentThread().toString() + " end summa thread");
        }

    }
}
