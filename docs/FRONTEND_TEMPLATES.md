# Templates frontend HireHub (gratuits)

## Stack « for free »

| Élément | Rôle | Licence |
|---------|------|---------|
| **Thymeleaf** | Moteur de vues HTML côté serveur | Apache 2.0 |
| **Thymeleaf Layout Dialect** | Page maître `layout.html` + `layout:decorate` | Apache 2.0 |
| **Bootstrap 5** (CDN jsDelivr) | CSS / composants responsive | MIT |
| **Chart.js** (CDN) | Graphiques dashboard admin | MIT |
| **Feuille `static/css/app.css`** | Tes surcharges locales | — |

Aucun template payant : tout est open source ou CDN public.

## Fichiers clés

- `templates/layout.html` — structure commune (head, nav, footer, scripts).
- `templates/fragments/header.html` — barre de navigation (`th:fragment="header"`).
- `templates/fragments/footer.html` — pied de page.
- `templates/pages/*.html` — une page métier = `layout:decorate="~{layout}"` + fragment `content`.

## Nouvelle page

1. Créer `templates/pages/ma-page.html` en copiant une page existante sous `pages/`.
2. Déclarer `layout:decorate="~{layout}"` et remplir `<section layout:fragment="content">`.
3. Ajouter un `@GetMapping` dans un `@Controller` qui retourne `"pages/ma-page"`.

## Titre dans l’onglet

Dans le `<head>` de la page : `<title>Mon titre</title>` → affiché comme **Mon titre - HireHub** (voir `layout:title-pattern`).
