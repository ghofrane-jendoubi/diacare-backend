package tn.esprit.spring.diacarebackend.Service;

import tn.esprit.spring.diacarebackend.entities.ChatbotConversation;
import tn.esprit.spring.diacarebackend.entities.ChatbotMessage;
import tn.esprit.spring.diacarebackend.repository.ChatbotConversationRepository;
import tn.esprit.spring.diacarebackend.repository.ChatbotMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatbotService {

    private final ChatbotConversationRepository convRepo;
    private final ChatbotMessageRepository msgRepo;

    public ChatbotService(ChatbotConversationRepository convRepo,
                          ChatbotMessageRepository msgRepo) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
    }

    @Transactional
    public Map<String, Object> chat(String sessionId, String userMessage, Long patientId) {

        ChatbotConversation conv = convRepo.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatbotConversation c = new ChatbotConversation();
                    c.setSessionId(sessionId);
                    c.setPatientId(patientId);
                    return convRepo.save(c);
                });

        ChatbotMessage patientMsg = new ChatbotMessage();
        patientMsg.setConversationId(conv.getId());
        patientMsg.setSender(ChatbotMessage.Sender.PATIENT);
        patientMsg.setMessage(userMessage);
        msgRepo.save(patientMsg);

        String botResponse = generateResponse(userMessage.toLowerCase().trim());

        ChatbotMessage botMsg = new ChatbotMessage();
        botMsg.setConversationId(conv.getId());
        botMsg.setSender(ChatbotMessage.Sender.BOT);
        botMsg.setMessage(botResponse);
        msgRepo.save(botMsg);

        return Map.of(
                "sessionId", sessionId,
                "response", botResponse,
                "conversationId", conv.getId()
        );
    }

    public List<ChatbotMessage> getHistory(String sessionId) {
        return convRepo.findBySessionId(sessionId)
                .map(c -> msgRepo.findByConversationIdOrderByCreatedAtAsc(c.getId()))
                .orElse(new ArrayList<>());
    }

    private String generateResponse(String message) {

        // ===== SALUTATIONS =====
        if (containsAny(message, "bonjour", "bonsoir", "salut", "hello", "hi", "coucou")) {
            return "👋 Bonjour ! Je suis DiaCare Assistant, votre guide santé pour la gestion du diabète.\n\n" +
                    "Comment puis-je vous aider aujourd'hui ?\n\n" +
                    "Vous pouvez me poser des questions sur :\n" +
                    "• 🩸 La glycémie et sa surveillance\n" +
                    "• 🥗 L'alimentation et la nutrition\n" +
                    "• 💊 Les médicaments\n" +
                    "• 🏃 L'activité physique\n" +
                    "• 🧠 Le stress et le diabète\n" +
                    "• 🆘 Les urgences diabétiques";
        }

        // ===== GLYCÉMIE =====
        if (containsAny(message, "glycémie", "glycemie", "sucre", "glucose",
                "taux", "mesure", "dextro", "lecteur", "piqûre", "piqure")) {

            // Mesurer / surveiller
            if (containsAny(message, "mesurer", "mesure", "comment", "quand",
                    "fréquence", "souvent", "fois", "moment", "heure")) {
                return "📊 **Quand et comment mesurer sa glycémie ?**\n\n" +
                        "**Moments recommandés :**\n" +
                        "• Le matin à jeun (avant tout repas)\n" +
                        "• Avant chaque repas principal\n" +
                        "• 2 heures après les repas\n" +
                        "• Avant de se coucher\n" +
                        "• Avant/après l'exercice physique\n" +
                        "• En cas de malaise\n\n" +
                        "**Comment faire :**\n" +
                        "1. Lavez-vous les mains à l'eau tiède\n" +
                        "2. Séchez bien\n" +
                        "3. Piquez le côté du doigt (moins douloureux)\n" +
                        "4. Posez la goutte sur la bandelette\n" +
                        "5. Lisez le résultat après 5 secondes\n\n" +
                        "💡 Notez chaque résultat dans votre carnet de suivi.";
            }

            // Valeurs normales
            if (containsAny(message, "normal", "normale", "cible", "idéal", "ideal",
                    "valeur", "objectif", "chiffre", "résultat", "resultat",
                    "combien", "quel", "quelle")) {
                return "📊 **Valeurs de glycémie de référence :**\n\n" +
                        "**Personne non diabétique :**\n" +
                        "• À jeun : 0,70 – 1,00 g/L\n" +
                        "• Après repas : < 1,40 g/L\n\n" +
                        "**Diabétique traité (objectifs) :**\n" +
                        "• À jeun : 0,80 – 1,30 g/L\n" +
                        "• Après repas : < 1,80 g/L\n" +
                        "• HbA1c : < 7%\n\n" +
                        "**Seuils d'alerte :**\n" +
                        "• < 0,70 g/L → Hypoglycémie\n" +
                        "• > 2,50 g/L → Hyperglycémie sévère\n" +
                        "• > 3,00 g/L → Urgence médicale\n\n" +
                        "⚠️ Ces valeurs peuvent varier selon votre médecin.";
            }

            // Hyperglycémie
            if (containsAny(message, "haute", "élevé", "eleve", "hyper", "hyperglycémie",
                    "hyperglycemie", "monte", "dépasse", "depasse", "trop haut",
                    "augmente", "grimpe", "2g", "3g", "2,", "3,")) {
                return "⚠️ **Hyperglycémie — Glycémie trop haute**\n\n" +
                        "**Signes reconnaissables :**\n" +
                        "• Soif intense et bouche sèche\n" +
                        "• Urines fréquentes et abondantes\n" +
                        "• Fatigue inhabituelle\n" +
                        "• Vision floue\n" +
                        "• Maux de tête\n\n" +
                        "**Causes fréquentes :**\n" +
                        "• Repas trop riche en glucides\n" +
                        "• Oubli du médicament ou de l'insuline\n" +
                        "• Stress ou infection\n" +
                        "• Manque d'exercice\n\n" +
                        "**Que faire immédiatement :**\n" +
                        "1. Buvez 1 à 2 verres d'eau\n" +
                        "2. Marchez 15-20 minutes\n" +
                        "3. Prenez votre traitement si prescrit\n" +
                        "4. Remesurer dans 2 heures\n\n" +
                        "🚨 Si > 3 g/L avec malaise : **appelez le 15**";
            }

            // Hypoglycémie
            if (containsAny(message, "basse", "faible", "hypo", "hypoglycémie",
                    "hypoglycemie", "descend", "tombe", "bas", "trop bas",
                    "0,", "tremblement", "sueur", "pâle", "pale")) {
                return "🚨 **Hypoglycémie — Glycémie trop basse**\n\n" +
                        "**Signes d'alerte :**\n" +
                        "• Tremblements, mains qui tremblent\n" +
                        "• Sueurs froides, pâleur\n" +
                        "• Faim soudaine et intense\n" +
                        "• Palpitations, cœur qui bat vite\n" +
                        "• Confusion, difficultés à parler\n\n" +
                        "**Resucrage immédiat :**\n" +
                        "1. Prenez 15g de glucides rapides :\n" +
                        "   → 3 sucres\n" +
                        "   → 1 verre de jus de fruit\n" +
                        "   → 1 canette de soda sucré\n" +
                        "2. Attendez 15 minutes\n" +
                        "3. Remesurer la glycémie\n" +
                        "4. Si toujours < 0,70 : recommencer\n\n" +
                        "⚠️ Toujours avoir du sucre sur soi !\n" +
                        "🚨 Si inconscient : appelez le **15**";
            }

            // Glycémie après repas
            if (containsAny(message, "après repas", "apres repas", "postprandial",
                    "après manger", "apres manger", "repas")) {
                return "🍽️ **Glycémie après les repas**\n\n" +
                        "**Valeurs normales :**\n" +
                        "• 1h après : peut monter jusqu'à 1,80 g/L\n" +
                        "• 2h après : doit être < 1,40 g/L\n" +
                        "• 3h après : retour à la valeur d'avant repas\n\n" +
                        "**Pour limiter les pics :**\n" +
                        "• Mangez lentement (minimum 20 min)\n" +
                        "• Commencez par les légumes\n" +
                        "• Évitez les sucres rapides\n" +
                        "• Marchez 10-15 min après le repas\n" +
                        "• Évitez les boissons sucrées\n\n" +
                        "💡 Le pic glycémique maximum survient 45-60 min après le repas.";
            }

            // Glycémie à jeun
            if (containsAny(message, "jeun", "matin", "réveil", "reveil", "nuit")) {
                return "🌅 **Glycémie à jeun (le matin)**\n\n" +
                        "**Définition :** mesure après 8h sans manger\n\n" +
                        "**Valeurs :**\n" +
                        "• Normale : < 1,00 g/L\n" +
                        "• Pré-diabète : 1,00 – 1,25 g/L\n" +
                        "• Diabète : ≥ 1,26 g/L (confirmé deux fois)\n\n" +
                        "**Pourquoi elle peut être haute le matin ?**\n" +
                        "• Phénomène de l'aube : libération d'hormones entre 4h et 8h\n" +
                        "• Effet Somogyi : hyperglycémie rebond après hypo nocturne\n\n" +
                        "**Conseils :**\n" +
                        "• Mesurez toujours à la même heure\n" +
                        "• Ne buvez que de l'eau avant la mesure\n" +
                        "• Notez les valeurs dans un carnet";
            }

            // Glycémie et stress
            if (containsAny(message, "stress", "anxiété", "anxiete", "émotions",
                    "emotions", "peur", "colère", "colere")) {
                return "🧠 **Stress et glycémie**\n\n" +
                        "Le stress fait monter la glycémie car le corps libère :\n" +
                        "• Adrénaline → libère du glucose stocké\n" +
                        "• Cortisol → réduit l'efficacité de l'insuline\n\n" +
                        "**Résultat :** la glycémie peut monter de 0,50 à 1 g/L\n" +
                        "pendant un épisode de stress intense.\n\n" +
                        "**Gérer le stress pour stabiliser la glycémie :**\n" +
                        "• Respiration profonde (4-7-8)\n" +
                        "• Marche de 10-15 min\n" +
                        "• Yoga ou méditation\n" +
                        "• Sommeil suffisant (7-8h)\n" +
                        "• Technique de relaxation musculaire\n\n" +
                        "💡 Notez votre niveau de stress à côté de vos mesures.";
            }

            // Glycémie et exercice
            if (containsAny(message, "sport", "exercice", "activité", "marche",
                    "gym", "courir", "natation", "vélo", "velo")) {
                return "🏃 **Glycémie et activité physique**\n\n" +
                        "**Avant l'exercice :**\n" +
                        "• Mesurez la glycémie\n" +
                        "• Si < 1,00 g/L → prenez une collation\n" +
                        "• Si > 2,50 g/L → évitez l'exercice intense\n\n" +
                        "**Pendant l'exercice :**\n" +
                        "• L'exercice fait baisser la glycémie\n" +
                        "• Buvez de l'eau régulièrement\n" +
                        "• Ayez toujours du sucre avec vous\n\n" +
                        "**Après l'exercice :**\n" +
                        "• La glycémie peut continuer à baisser 2-4h après\n" +
                        "• Mesurez 30 min et 2h après l'arrêt\n" +
                        "• Prenez une collation si nécessaire\n\n" +
                        "✅ L'exercice régulier améliore la sensibilité à l'insuline !";
            }

            // Glycémie et alimentation
            if (containsAny(message, "manger", "aliment", "nourriture", "nutrition",
                    "régime", "regime", "pain", "riz", "fruit")) {
                return "🥗 **Alimentation et glycémie**\n\n" +
                        "**Aliments qui font monter rapidement (IG élevé) :**\n" +
                        "• Pain blanc, baguette\n" +
                        "• Riz blanc, pommes de terre\n" +
                        "• Sodas, jus de fruits industriels\n" +
                        "• Bonbons, chocolat au lait\n\n" +
                        "**Aliments qui font monter lentement (IG bas) :**\n" +
                        "• Légumineuses (lentilles, pois chiches)\n" +
                        "• Légumes verts\n" +
                        "• Pain complet, riz complet\n" +
                        "• Yaourt nature, fromage\n\n" +
                        "**Astuce ordre de consommation :**\n" +
                        "Légumes → Protéines → Glucides\n" +
                        "→ Réduit le pic glycémique de 30% !";
            }

            // Glycémie et insuline
            if (containsAny(message, "insuline", "injection", "stylo", "unité",
                    "dose", "basal", "bolus", "rapide", "lente")) {
                return "💉 **Insuline et glycémie**\n\n" +
                        "**Types d'insuline :**\n\n" +
                        "🔵 **Insuline rapide (bolus)**\n" +
                        "→ Action en 15-30 min\n" +
                        "→ Pour couvrir les repas\n" +
                        "→ Dure 3-5 heures\n\n" +
                        "🟢 **Insuline lente (basale)**\n" +
                        "→ Action en 1-2h\n" +
                        "→ Couvre les besoins de base\n" +
                        "→ Dure 12-24 heures\n\n" +
                        "**Zones d'injection :**\n" +
                        "• Ventre (absorption la plus rapide)\n" +
                        "• Cuisses (plus lente)\n" +
                        "• Bras, fesses\n\n" +
                        "⚠️ Ne modifiez jamais votre dose sans consulter votre médecin !";
            }

            // Carnet de suivi
            if (containsAny(message, "noter", "note", "carnet", "journal", "suivi",
                    "enregistrer", "historique", "données")) {
                return "📓 **Carnet de suivi glycémique**\n\n" +
                        "**Que noter à chaque mesure :**\n" +
                        "• Date et heure\n" +
                        "• Valeur de la glycémie\n" +
                        "• Moment (à jeun, avant/après repas)\n" +
                        "• Ce que vous avez mangé\n" +
                        "• Activité physique\n" +
                        "• Médicaments pris\n" +
                        "• Événements (stress, maladie)\n\n" +
                        "**Applications recommandées :**\n" +
                        "• mySugr (iOS/Android)\n" +
                        "• Gluci-Check\n" +
                        "• DiabeteM\n\n" +
                        "💡 Apportez votre carnet à chaque consultation médicale !";
            }

            // Glycémie — réponse générale
            return "🩸 **Glycémie — Que voulez-vous savoir ?**\n\n" +
                    "Je peux vous expliquer :\n\n" +
                    "• 📊 **Valeurs normales** → tapez 'valeurs normales'\n" +
                    "• ⬆️ **Glycémie trop haute** → tapez 'glycémie élevée'\n" +
                    "• ⬇️ **Glycémie trop basse** → tapez 'hypoglycémie'\n" +
                    "• 🕐 **Quand mesurer** → tapez 'comment mesurer'\n" +
                    "• 🍽️ **Après les repas** → tapez 'glycémie après repas'\n" +
                    "• 🌅 **Le matin à jeun** → tapez 'glycémie à jeun'\n" +
                    "• 🧠 **Stress et glycémie** → tapez 'stress glycémie'\n" +
                    "• 🏃 **Sport et glycémie** → tapez 'sport glycémie'\n" +
                    "• 💉 **Insuline** → tapez 'insuline'\n" +
                    "• 📓 **Carnet de suivi** → tapez 'carnet'";
        }

        // ===== HBA1C =====
        if (containsAny(message, "hba1c", "hémoglobine", "hemoglobine", "a1c")) {
            return "🔬 **HbA1c (Hémoglobine glyquée)**\n\n" +
                    "C'est un bilan sanguin qui mesure votre glycémie moyenne sur **3 mois**.\n\n" +
                    "**Objectifs :**\n" +
                    "• < 7% : excellent contrôle\n" +
                    "• 7% – 8% : acceptable\n" +
                    "• > 8% : insuffisant, adapter le traitement\n\n" +
                    "**Fréquence recommandée :**\n" +
                    "• Tous les 3 mois si traitement modifié\n" +
                    "• Tous les 6 mois si diabète équilibré\n\n" +
                    "💡 Ce test est remboursé à 100% pour les diabétiques.";
        }

        // ===== ALIMENTATION =====
        if (containsAny(message, "manger", "alimentation", "nutrition", "régime",
                "repas", "nourriture", "aliment", "diet", "menu")) {
            if (containsAny(message, "interdit", "éviter", "pas manger", "ne pas")) {
                return "🚫 **Aliments à limiter avec le diabète :**\n\n" +
                        "• Sucres rapides : bonbons, sodas, jus industriels\n" +
                        "• Pain blanc, farine blanche raffinée\n" +
                        "• Alcool (déstabilise la glycémie)\n" +
                        "• Aliments très gras et frits\n" +
                        "• Céréales sucrées du petit-déjeuner\n\n" +
                        "✅ **À privilégier :**\n" +
                        "• Légumes verts, légumineuses\n" +
                        "• Céréales complètes\n" +
                        "• Viandes maigres, poissons\n" +
                        "• Huile d'olive\n\n" +
                        "💡 Consultez notre espace éducatif pour des menus complets !";
            }
            return "🥗 **Alimentation et diabète**\n\n" +
                    "Les règles d'or :\n" +
                    "1. **3 repas équilibrés** par jour\n" +
                    "2. **Légumes** à chaque repas (moitié de l'assiette)\n" +
                    "3. **Glucides complexes** (pain complet, riz complet)\n" +
                    "4. **Protéines maigres** (poulet, poisson, œufs)\n" +
                    "5. **Éviter** les sucres rapides\n" +
                    "6. **Boire** 1,5 à 2L d'eau par jour\n\n" +
                    "Voulez-vous savoir quels aliments éviter ?";
        }

        // ===== MÉDICAMENTS =====
        if (containsAny(message, "médicament", "medicament", "insuline", "metformine",
                "traitement", "comprimé", "injection", "stylo")) {
            return "💊 **Médicaments du diabète**\n\n" +
                    "🔵 **Metformine (Glucophage)**\n" +
                    "→ Premier traitement du diabète type 2\n" +
                    "→ Se prend pendant les repas\n\n" +
                    "🟡 **Sulfamides hypoglycémiants**\n" +
                    "→ Stimulent la production d'insuline\n" +
                    "→ Risque d'hypoglycémie\n\n" +
                    "🔴 **Insuline**\n" +
                    "→ Indispensable pour le type 1\n" +
                    "→ Injection sous-cutanée\n\n" +
                    "⚠️ Ne jamais arrêter votre traitement sans avis médical.";
        }

        // ===== EXERCICE =====
        if (containsAny(message, "sport", "exercice", "activité", "marche",
                "gym", "bouger", "physique")) {
            return "🏃 **Activité physique et diabète**\n\n" +
                    "**Bénéfices :**\n" +
                    "• Baisse la glycémie naturellement\n" +
                    "• Améliore la sensibilité à l'insuline\n" +
                    "• Réduit le risque cardiovasculaire\n\n" +
                    "**Recommandations :**\n" +
                    "• 30 min de marche rapide, 5 fois/semaine\n" +
                    "• Ou 150 min d'activité modérée/semaine\n\n" +
                    "⚠️ **Précautions :**\n" +
                    "• Mesurer la glycémie avant l'effort\n" +
                    "• Avoir du sucre avec soi\n" +
                    "• Éviter si glycémie > 2,5 g/L";
        }

        // ===== URGENCES =====
        if (containsAny(message, "urgence", "samu", "15", "secours", "danger")) {
            return "🚨 **Urgences diabétiques**\n\n" +
                    "**Appeler le 15 (SAMU) si :**\n" +
                    "• Perte de connaissance\n" +
                    "• Glycémie > 3 g/L avec malaise\n" +
                    "• Hypoglycémie sévère\n" +
                    "• Vomissements répétés\n\n" +
                    "**Numéros utiles :**\n" +
                    "📞 SAMU : **15**\n" +
                    "📞 Pompiers : **18**\n" +
                    "📞 Urgences EU : **112**";
        }

        // ===== RENDEZ-VOUS =====
        if (containsAny(message, "médecin", "docteur", "rendez-vous", "consultation", "rdv")) {
            return "👨‍⚕️ **Suivi médical recommandé**\n\n" +
                    "**Consultations régulières :**\n" +
                    "• Médecin généraliste : tous les 3 mois\n" +
                    "• Endocrinologue : 1 à 2 fois/an\n" +
                    "• Cardiologue : annuel\n" +
                    "• Ophtalmologue : annuel (fond d'œil)\n" +
                    "• Podologue : annuel (soins des pieds)\n" +
                    "• Dentiste : 2 fois/an\n\n" +
                    "📱 Prenez rendez-vous depuis l'application DiaCare !";
        }

        // ===== REMERCIEMENTS =====
        if (containsAny(message, "merci", "thank", "bravo", "super", "parfait")) {
            return "😊 De rien ! Je suis là pour vous aider.\n\n" +
                    "N'hésitez pas à me poser d'autres questions. " +
                    "Je reste disponible 24h/24 💙";
        }

        // ===== AU REVOIR =====
        if (containsAny(message, "au revoir", "bye", "à bientôt", "bonne journée")) {
            return "👋 Au revoir ! Prenez bien soin de vous.\n\n" +
                    "N'oubliez pas :\n" +
                    "• Surveiller votre glycémie régulièrement\n" +
                    "• Prendre vos médicaments\n" +
                    "• Rester actif !\n\n" +
                    "À bientôt sur DiaCare 💙";
        }

        // ===== AIDE =====
        if (containsAny(message, "aide", "help", "menu", "que", "quoi",
                "comment", "qu'est", "explique")) {
            return "🤖 **DiaCare Assistant** — Je peux vous aider sur :\n\n" +
                    "🩸 **Glycémie** → valeurs, hypo, hyperglycémie, mesure\n" +
                    "🥗 **Nutrition** → alimentation, régime, menus\n" +
                    "💊 **Médicaments** → insuline, metformine, traitements\n" +
                    "🏃 **Sport** → exercice, activité physique\n" +
                    "🔬 **HbA1c** → bilan sanguin, suivi\n" +
                    "👨‍⚕️ **Médecin** → consultations, suivi médical\n" +
                    "🚨 **Urgences** → que faire en cas de crise\n\n" +
                    "Tapez votre question !";
        }

        // ===== RÉPONSE PAR DÉFAUT =====
        return "🤖 Je ne suis pas sûr de comprendre votre question.\n\n" +
                "Voici ce que je peux vous expliquer :\n" +
                "• 🩸 Glycémie et surveillance\n" +
                "• 🥗 Alimentation adaptée\n" +
                "• 💊 Médicaments\n" +
                "• 🏃 Activité physique\n" +
                "• 🚨 Urgences\n\n" +
                "Tapez **'aide'** pour voir toutes les options.";
    }

    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }
}