package com.csit228.capstone.model;

import java.util.ArrayList;
import java.util.List;

public class Department {
    private String name;
    private List<Job> jobs = new ArrayList<>();

    public Department(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}
