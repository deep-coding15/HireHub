package com.hirehub.frontend.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Attributs UI pour la démo Thymeleaf (parcours recruteur, menus conditionnels).
 * <p>
 * Ajoutez {@code ?demo=} à l’URL pour simuler un état :
 * {@code visiteur} | {@code connecte} | {@code demande_attente} | {@code demande_rejetee} | {@code recruteur} | {@code admin}
 * </p>
 */
@ControllerAdvice(basePackages = "com.hirehub.frontend.web")
public class DemoUiAdvice {

    @ModelAttribute
    public void uiDemoState(HttpServletRequest request, Model model) {
        String demo = request.getParameter("demo");
        if (demo == null || demo.isBlank()) {
            demo = "connecte";
        }

        boolean visiteur = "visiteur".equals(demo);
        boolean connecte = !visiteur;
        boolean estRecruteur = "recruteur".equals(demo);
        boolean demandeEnAttente = "demande_attente".equals(demo);
        boolean demandeRejetee = "demande_rejetee".equals(demo);
        boolean estAdmin = "admin".equals(demo);

        boolean peutSoumettreDemande = connecte && !estRecruteur && !demandeEnAttente;

        model.addAttribute("uiConnecte", connecte);
        model.addAttribute("uiEstRecruteur", estRecruteur);
        model.addAttribute("uiDemandeEnAttente", demandeEnAttente);
        model.addAttribute("uiDemandeRejetee", demandeRejetee);
        model.addAttribute("uiEstAdmin", estAdmin);
        model.addAttribute("uiPeutSoumettreDemandeRecruteur", peutSoumettreDemande);
        model.addAttribute("uiDemoMode", demo);
    }
}
