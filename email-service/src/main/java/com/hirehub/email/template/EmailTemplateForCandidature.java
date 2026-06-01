package com.hirehub.email.template;

public class EmailTemplateForCandidature {

    // ═══════════════════════════════════════════════════════════════
    // TEMPLATES HTML BUILDERS FOR CANDIDATURE-RELATED EMAILS
    // ═══════════════════════════════════════════════════════════════

    public static String buildCandidatureConfirmationTemplate(String candidatName, String offreTitle) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                    .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 20px; }
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>HireHub</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Merci de votre candidature pour le poste de <strong>%s</strong>.</p>
                        <p>Nous avons bien reçu votre dossier et l'examinerons attentivement.</p>
                        <p>Vous recevrez une mise à jour prochainement.</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 HireHub. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(candidatName, offreTitle);
    }

    public static String buildCandidatureStatutChangedTemplate(String candidatName, String offreTitle,
                                                               String ancienStatut, String nouveauStatut,
                                                               String commentaire) {
        String statut = nouveauStatut != null ? nouveauStatut.toUpperCase() : "";

        String statusColor = switch (statut) {
            case "ACCEPTEE"  -> "#10b981";
            case "REFUSEE"   -> "#ef4444";
            case "EN_COURS"  -> "#f59e0b";
            case "ENTRETIEN" -> "#6366f1";
            default          -> "#3b82f6";
        };

        String statusLabel = switch (statut) {
            case "ACCEPTEE"  -> "Candidature acceptée ✓";
            case "REFUSEE"   -> "Candidature non retenue";
            case "EN_COURS"  -> "En cours d'examen";
            case "ENTRETIEN" -> "Entretien programmé";
            case "SOUMISE"   -> "Soumise";
            default          -> nouveauStatut;
        };

        String statusMessage = switch (statut) {
            case "ACCEPTEE"  -> "Félicitations ! Votre candidature a été retenue.";
            case "REFUSEE"   -> "Nous vous remercions pour votre intérêt. Votre candidature n'a pas été retenue à cette étape.";
            case "EN_COURS"  -> "Votre dossier est en cours d'examen par le recruteur.";
            case "ENTRETIEN" -> "Bonne nouvelle ! Vous avez été sélectionné(e) pour un entretien. Vous recevrez prochainement les détails.";
            default          -> "Le statut de votre candidature a été mis à jour.";
        };

        String ancienLabel = ancienStatut != null ? switch (ancienStatut.toUpperCase()) {
            case "SOUMISE"   -> "Soumise";
            case "EN_COURS"  -> "En cours d'examen";
            case "ENTRETIEN" -> "Entretien programmé";
            case "ACCEPTEE"  -> "Acceptée";
            case "REFUSEE"   -> "Refusée";
            default          -> ancienStatut;
        } : null;

        String transitionHtml = (ancienLabel != null)
                ? "<p style=\"color:#6b7280;font-size:13px;\">Transition : <em>" + ancienLabel
                  + "</em> &rarr; <strong>" + statusLabel + "</strong></p>"
                : "";

        String commentaireHtml = (commentaire != null && !commentaire.isBlank())
                ? "<p><strong>Commentaire :</strong> " + commentaire + "</p>"
                : "";

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; overflow: hidden; }
                    .header { background-color: #2563eb; color: white; padding: 24px 20px; text-align: center; }
                    .header h1 { margin: 0; font-size: 22px; }
                    .status-badge { background-color: %s; color: white; padding: 14px 20px; text-align: center;
                                    border-radius: 8px; margin: 16px 0; font-weight: bold; font-size: 17px; }
                    .info-block { background-color: #f8fafc; border-left: 4px solid #e2e8f0;
                                  padding: 12px 16px; border-radius: 0 6px 6px 0; margin: 12px 0; }
                    .content { padding: 24px; }
                    .footer { background-color: #f9fafb; padding: 14px; text-align: center;
                               font-size: 12px; color: #9ca3af; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>HireHub — Mise à jour de candidature</h1></div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <div class="status-badge">%s</div>
                        <p>%s</p>
                        <div class="info-block">
                            <p style="margin:4px 0;"><strong>Offre :</strong> %s</p>
                            %s
                        </div>
                        %s
                        <p style="margin-top:20px;">Cordialement,<br><strong>L'équipe HireHub</strong></p>
                    </div>
                    <div class="footer"><p>© 2026 HireHub. Tous droits réservés.</p></div>
                </div>
            </body>
            </html>
            """.formatted(statusColor, candidatName, statusLabel, statusMessage,
                          offreTitle != null ? offreTitle : "—",
                          transitionHtml, commentaireHtml);
    }

    public static String buildCandidatureEntretienTemplate(String candidatName, String offreTitle,
                                                           String dateEntretien, String lieux, String consignes) {
        String consignesHtml = (consignes != null && !consignes.isBlank())
            ? "<div class=\"detail-item\"><strong>📝 Consignes :</strong><br>" + consignes + "</div>"
            : "";
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                    .header { background-color: #4c1d95; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .details { background-color: #ede9fe; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .detail-item { margin: 10px 0; }
                    .content { padding: 20px; }
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h1>🎉 Entretien planifié — HireHub</h1></div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Nous sommes heureux de vous inviter à un entretien pour le poste de <strong>%s</strong>.</p>
                        <div class="details">
                            <div class="detail-item"><strong>📅 Date et heure :</strong> %s</div>
                            <div class="detail-item"><strong>📍 Lieu / Lien :</strong> %s</div>
                            %s
                        </div>
                        <p>Bonne préparation et à très bientôt !</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer"><p>© 2026 HireHub. Tous droits réservés.</p></div>
                </div>
            </body>
            </html>
            """.formatted(candidatName, offreTitle, dateEntretien, lieux, consignesHtml);
    }

    public static String buildCandidatureEntretienAnnulationTemplate(String candidatName, String offreTitle, String raison) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                    .header { background-color: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 20px; }
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Annulation d'entretien</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Nous devons malheureusement annuler l'entretien prévu pour le poste de <strong>%s</strong>.</p>
                        %s
                        <p>Nous nous excusons pour le désagrément et vous recontacterons si de nouvelles opportunités se présentent.</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 HireHub. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(candidatName, offreTitle,
                raison != null && !raison.isBlank()
                        ? "<p><strong>Raison:</strong> " + raison + "</p>"
                        : "");
    }

}
