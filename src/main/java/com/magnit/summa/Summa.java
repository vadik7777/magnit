/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.magnit.summa;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author KonovalovVA
 */
public class Summa implements Runnable {

    private String url;//адрес базы данных
    private String user;//пользователь базы данных
    private String password;//пароль от базы данных
    private String driver; // драйвер для подключения
    private int n; // число N из задания
    private int thread = 10;//количество потоков

    public static void main(String[] args) throws InterruptedException {
        System.out.println("hello from Ekaterinburg!");

        long mili = System.currentTimeMillis();

        Summa sum = new Summa();
        sum.setUrl("jdbc:postgresql://localhost:5432/magnit");
        //драйвер обязательно должен быть подключен или прописан в pom.xml
        sum.setDriver("org.postgresql.Driver");
        sum.setUser("postgres");
        sum.setPassword("postgres");
        sum.setN(1000000);
        sum.setThread(90);
        Thread sm = new Thread(sum);
        sm.start();
        sm.join();
        System.out.println(("Время выполнения " + (System.currentTimeMillis() - mili) / 1000) + " c.");
    }

    @Override
    public void run() {
        DBPool pool = null;
        //Создаем подключение к базе данных;
        try {
            pool = new DBPool(url, user, password, driver);
        } catch (ClassNotFoundException ex) {
            System.out.println("Проблемы с драйвером базы данных");
            ex.printStackTrace();
            return;
        }
        //Проверка подключения к базе данных
        try {
            pool.putConnection(pool.getConnection());

        } catch (SQLException ex) {
            System.out.println("Ошибка соединения с базой данных");
            ex.printStackTrace();
            return;
        }

        //Запуск и ожидание потока удаления элементов из БД
        Thread delete = new Thread(new TestThread(pool, "delete"));
        delete.start();
        try {
            delete.join();
        } catch (InterruptedException ex) {
            System.out.println("Ошибка остановки потока");
            ex.printStackTrace();
        }

        //Проверка входных данных для вставки
        int in;
        if (thread < 1) {
            thread = 1;
        }
        if (n < 1) {
            n = 1;
        }
        if (n < thread) {
            thread = 1;
            in = n;
        }

        //Расчет количества элементов для потока вставки (можно улучшить)
        if (n % thread == 0) {
            in = n / thread;
        } else {
            if (thread != 1) {
                in = n / (thread - 1);
            } else {
                in = n;
            }
        }

        //Запуск потоков вставки
        Thread[] allThreads = new Thread[thread];
        for (int i = 0; i < allThreads.length; i++) {
            if (i == 0) {
                allThreads[i] = new Thread(new TestThread(1, in, pool, "insert"));
            } else if (i == thread - 1) {
                allThreads[i] = new Thread(new TestThread(in * i + 1, n, pool, "insert"));
            } else {
                allThreads[i] = new Thread(new TestThread(in * i + 1, in * (i + 1), pool, "insert"));
            }
            allThreads[i].start();
        }

        //Ожидание окончания потоков вставки
        for (int i = 0; i < allThreads.length; i++) {
            if (allThreads[i] != null) {
                if (allThreads[i].isAlive()) {
                    try {
                        allThreads[i].join();
                    } catch (InterruptedException ex) {
                        System.out.println("Ошибка остановки потока");
                        ex.printStackTrace();
                    }
                }
            }
        }

        //Запуск и ожидание потока записи в файл 1.xml
        Thread writeXML = new Thread(new TestThread(pool, "xml"));
        writeXML.start();
        try {
            writeXML.join();
        } catch (InterruptedException ex) {
            System.out.println("Ошибка остановки потока");
            ex.printStackTrace();
        }
        //Запуск и ожидание потока записи в файл 2.xml
        Thread writeXSL = new Thread(new TestThread("xsl"));
        writeXSL.start();
        try {
            writeXSL.join();
        } catch (InterruptedException ex) {
            System.out.println("Ошибка остановки потока");
            ex.printStackTrace();
        }
        //Запуск и ожидание потока подсчета суммы из файла 2.xml
        Thread summa = new Thread(new TestThread("sum"));
        summa.start();
        try {
            summa.join();
        } catch (InterruptedException ex) {
            System.out.println("Ошибка остановки потока");
            ex.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

}
