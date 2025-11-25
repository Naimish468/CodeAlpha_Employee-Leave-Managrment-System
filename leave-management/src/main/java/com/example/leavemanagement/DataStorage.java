package com.example.leavemanagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class DataStorage {
    private static final String EMPLOYEES_FILE = "employees.json";
    private static final String LEAVES_FILE = "leaves.json";
    private Gson gson = new Gson();

    public List<Employee> loadEmployees() {
        try (Reader reader = new FileReader(EMPLOYEES_FILE)) {
            Type listType = new TypeToken<List<Employee>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveEmployees(List<Employee> employees) {
        try (Writer writer = new FileWriter(EMPLOYEES_FILE)) {
            gson.toJson(employees, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<LeaveApplication> loadLeaves() {
        try (Reader reader = new FileReader(LEAVES_FILE)) {
            Type listType = new TypeToken<List<LeaveApplication>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveLeaves(List<LeaveApplication> leaves) {
        try (Writer writer = new FileWriter(LEAVES_FILE)) {
            gson.toJson(leaves, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}