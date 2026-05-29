package com.hirehub.email.template;

public final class EmailTemplateForAdminRecruiter {

    private EmailTemplateForAdminRecruiter() {}

    public static String buildAdminUserActionTemplate(String userName, String action, String role) {
        String title = switch (action) {
            case "BLOCKED" -> "Compte suspendu";
            case "UNBLOCKED" -> "Compte réactivé";
            case "DELETED" -> "Compte supprimé";
            default -> "Action sur votre compte";
        };
        String message = switch (action) {
            case "BLOCKED" -> "Votre compte HireHub a été suspendu par un administrateur. Vous ne pouvez plus vous connecter.";
            case "UNBLOCKED" -> "Votre compte HireHub a été réactivé. Vous pouvez à nouveau vous connecter.";
            case "DELETED" -> "Votre compte HireHub a été supprimé par un administrateur.";
            default -> "Une action administrative a été effectuée sur votre compte.";
        };
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head><meta charset="UTF-8"><style>
        body{font-family:Arial,sans-serif;background:#f5f5f5}
        .container{max-width:600px;margin:0 auto;background:#fff;padding:20px;border-radius:8px}
        .header{background:#dc2626;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0}
        </style></head>
        <body><div class="container">
        <div class="header"><h1>%s</h1></div>
        <p>Bonjour <strong>%s</strong>,</p>
        <p>%s</p>
        <p><strong>Rôle :</strong> %s</p>
        <p>Cordialement,<br>L'équipe HireHub</p>
        </div></body></html>
        """.formatted(title, userName, message, role);
    }

    public static String buildRecruiterApprovedTemplate(String userName) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head><meta charset="UTF-8"><style>
        body{font-family:Arial,sans-serif;background:#f5f5f5}
        .container{max-width:600px;margin:0 auto;background:#fff;padding:20px;border-radius:8px}
        .header{background:#059669;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0}
        </style></head>
        <body><div class="container">
        <div class="header"><h1>Inscription recruteur approuvée</h1></div>
        <p>Bonjour <strong>%s</strong>,</p>
        <p>Votre dossier recruteur a été validé. Vous pouvez publier des offres et gérer vos candidatures sur HireHub.</p>
        <p>Cordialement,<br>L'équipe HireHub</p>
        </div></body></html>
        """.formatted(userName);
    }

    public static String buildRecruiterRejectedTemplate(String userName, String reason) {
        String detail = reason != null && !reason.isBlank() ? reason : "Votre dossier ne répond pas aux critères requis.";
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head><meta charset="UTF-8"><style>
        body{font-family:Arial,sans-serif;background:#f5f5f5}
        .container{max-width:600px;margin:0 auto;background:#fff;padding:20px;border-radius:8px}
        .header{background:#b45309;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0}
        </style></head>
        <body><div class="container">
        <div class="header"><h1>Inscription recruteur refusée</h1></div>
        <p>Bonjour <strong>%s</strong>,</p>
        <p>%s</p>
        <p>Cordialement,<br>L'équipe HireHub</p>
        </div></body></html>
        """.formatted(userName, detail);
    }

    public static String buildRecruiterReviewRequiredTemplate(String userName, String message) {
        String detail = message != null && !message.isBlank()
                ? message
                : "Votre dossier est en cours de vérification manuelle par notre équipe.";
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head><meta charset="UTF-8"><style>
        body{font-family:Arial,sans-serif;background:#f5f5f5}
        .container{max-width:600px;margin:0 auto;background:#fff;padding:20px;border-radius:8px}
        .header{background:#2563eb;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0}
        </style></head>
        <body><div class="container">
        <div class="header"><h1>Vérification en cours</h1></div>
        <p>Bonjour <strong>%s</strong>,</p>
        <p>%s</p>
        <p>Cordialement,<br>L'équipe HireHub</p>
        </div></body></html>
        """.formatted(userName, detail);
    }
}
