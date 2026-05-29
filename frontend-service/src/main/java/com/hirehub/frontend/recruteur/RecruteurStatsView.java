package com.hirehub.frontend.recruteur;

import java.util.ArrayList;
import java.util.List;

public class RecruteurStatsView {

    private int offresCount;
    private long offresPubliees;
    private int candidaturesCount;
    private long tauxReponsePercent;
    private List<String> offreLabels = new ArrayList<>();
    private List<Integer> candidaturesParOffre = new ArrayList<>();

    public int getOffresCount() {
        return offresCount;
    }

    public void setOffresCount(int offresCount) {
        this.offresCount = offresCount;
    }

    public long getOffresPubliees() {
        return offresPubliees;
    }

    public void setOffresPubliees(long offresPubliees) {
        this.offresPubliees = offresPubliees;
    }

    public int getCandidaturesCount() {
        return candidaturesCount;
    }

    public void setCandidaturesCount(int candidaturesCount) {
        this.candidaturesCount = candidaturesCount;
    }

    public long getTauxReponsePercent() {
        return tauxReponsePercent;
    }

    public void setTauxReponsePercent(long tauxReponsePercent) {
        this.tauxReponsePercent = tauxReponsePercent;
    }

    public List<String> getOffreLabels() {
        return offreLabels;
    }

    public void setOffreLabels(List<String> offreLabels) {
        this.offreLabels = offreLabels != null ? offreLabels : new ArrayList<>();
    }

    public List<Integer> getCandidaturesParOffre() {
        return candidaturesParOffre;
    }

    public void setCandidaturesParOffre(List<Integer> candidaturesParOffre) {
        this.candidaturesParOffre = candidaturesParOffre != null ? candidaturesParOffre : new ArrayList<>();
    }

    public String getChartLabelsJson() {
        if (offreLabels.isEmpty()) {
            return "[\"Aucune offre\"]";
        }
        return toJsonArray(offreLabels);
    }

    public String getChartDataJson() {
        if (candidaturesParOffre.isEmpty()) {
            return "[0]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < candidaturesParOffre.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(candidaturesParOffre.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String toJsonArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(values.get(i).replace("\"", "\\\"")).append('"');
        }
        sb.append(']');
        return sb.toString();
    }
}
