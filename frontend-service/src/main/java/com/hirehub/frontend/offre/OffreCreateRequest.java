package com.hirehub.frontend.offre;

/**
 * Corps JSON attendu par offre-service (POST /api/offres).
 */
public class OffreCreateRequest {

    private String titre;
    private String description;
    private String typeContrat;
    private String ville;
    private Double salaire;
    private String dateExpiration;

    public static OffreCreateRequest fromForm(OffreForm form) {
        OffreCreateRequest request = new OffreCreateRequest();
        request.setTitre(form.getTitre());
        request.setDescription(form.getDescription());
        request.setTypeContrat(form.getTypeContrat());
        request.setVille(form.getVille());
        request.setSalaire(form.getSalaire());
        String date = form.getDateExpiration();
        if (date != null && !date.isBlank() && date.length() == 16) {
            date = date + ":00";
        }
        request.setDateExpiration(date);
        return request;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Double getSalaire() {
        return salaire;
    }

    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public String getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(String dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
}
