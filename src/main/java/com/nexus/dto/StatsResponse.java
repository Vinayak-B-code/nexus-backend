package com.nexus.dto;

public class StatsResponse {

    private long totalDevelopers;
    private long totalProjects;
    private long rescueProjects;
    private long totalCircles;
    private long totalBounties;
    private long totalConnections;

    public StatsResponse() {}

    public StatsResponse(long totalDevelopers, long totalProjects, long rescueProjects, long totalCircles, long totalBounties, long totalConnections) {
        this.totalDevelopers = totalDevelopers;
        this.totalProjects = totalProjects;
        this.rescueProjects = rescueProjects;
        this.totalCircles = totalCircles;
        this.totalBounties = totalBounties;
        this.totalConnections = totalConnections;
    }

    public long getTotalDevelopers() { return totalDevelopers; }
    public void setTotalDevelopers(long totalDevelopers) { this.totalDevelopers = totalDevelopers; }
    public long getTotalProjects() { return totalProjects; }
    public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }
    public long getRescueProjects() { return rescueProjects; }
    public void setRescueProjects(long rescueProjects) { this.rescueProjects = rescueProjects; }
    public long getTotalCircles() { return totalCircles; }
    public void setTotalCircles(long totalCircles) { this.totalCircles = totalCircles; }
    public long getTotalBounties() { return totalBounties; }
    public void setTotalBounties(long totalBounties) { this.totalBounties = totalBounties; }
    public long getTotalConnections() { return totalConnections; }
    public void setTotalConnections(long totalConnections) { this.totalConnections = totalConnections; }
}
