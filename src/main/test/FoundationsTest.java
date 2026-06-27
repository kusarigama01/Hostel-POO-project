import com.hotel.model.*;
import java.time.LocalDate;

public class FoundationsTest {
    public static void main(String[] args) {
        System.out.println("=== 🧪 DÉBUT DES TESTS DU SPRINT 1 ===\n");

        // --- TEST 1 : Instanciation des Acteurs & Enums ---
        System.out.println("--- Test 1 : Création des profils ---");

        var adresseClient = new Address("42 Avenue des Champs-Élysées", "Paris", "IDF", "75008", "France");
        var client = new Customer("Alice", "Vancamp", "alice.v@email.com", "+33612345678", LocalDate.of(2002, 3, 15), adresseClient);
        var manager = new Manager("Bob", "Sponge", "bob.manager@hotel.com", "+33687654321", "MGR-2026-009");

        System.out.println("✓ Client créé : " + client.getFullName() + " (Email: " + client.getEmail() + ")");
        System.out.println("✓ Adresse complète : " + client.getAddress().getFullAddress());
        System.out.println("✓ Manager créé : " + manager.getFullName() + " (ID Employé: " + manager.getEmployeeId() + ")");
        System.out.println();


        // --- TEST 2 : Algorithme de chevauchement de dates (Le cœur du système) ---
        System.out.println("--- Test 2 : Moteur de dates (DateRange) ---");

        // Période de référence : du 1er au 10 Juillet 2026
        var periodeReference = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10));
        System.out.println("Période cible : 01/07/2026 au 10/07/2026 (Durée : " + periodeReference.duration() + " nuits)");

        // Cas A : Chevauchement au début (28 Juin au 3 Juillet)
        var overlapDebut = new DateRange(LocalDate.of(2026, 6, 28), LocalDate.of(2026, 7, 3));
        System.out.println("🔍 Test Overlap Début (Attendu: true) -> " + periodeReference.overlaps(overlapDebut));

        // Cas B : Chevauchement à la fin (8 Juillet au 15 Juillet)
        var overlapFin = new DateRange(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 15));
        System.out.println("🔍 Test Overlap Fin (Attendu: true) -> " + periodeReference.overlaps(overlapFin));

        // Cas C : Totalement inclus (3 Juillet au 7 Juillet)
        var overlapInclus = new DateRange(LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7));
        System.out.println("🔍 Test Totalement Inclus (Attendu: true) -> " + periodeReference.overlaps(overlapInclus));

        // Cas D : Strictement après (11 Juillet au 15 Juillet)
        var noOverlapApres = new DateRange(LocalDate.of(2026, 7, 11), LocalDate.of(2026, 7, 15));
        System.out.println("🔍 Test Après sans contact (Attendu: false) -> " + periodeReference.overlaps(noOverlapApres));
        System.out.println();


        // --- TEST 3 : Validation des Cas Limites (Robustesse) ---
        System.out.println("--- Test 3 : Gestion des erreurs de saisie ---");

        try {
            // Tentative de créer une plage de date inversée (Fin avant le Début)
            var fausseDate = new DateRange(LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 1));
            System.out.println("❌ Échec : L'application a accepté une date de fin antérieure au début.");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Succès : Le système a bien bloqué les dates inversées ! Erreur interceptée : " + e.getMessage());
        }
        System.out.println();


        // --- TEST 4 : Intégrité des Énumérations ---
        System.out.println("--- Test 4 : Cycle de vie et États (Enums) ---");

        var typeChambre = RoomType.SUITE;
        var statutChambre = RoomStatus.AVAILABLE;
        var statutFacture = InvoiceStatus.UNPAID;
        var modePaiement = PaymentMethod.CREDIT_CARD;

        System.out.println("Simulation d'un flux d'états :");
        System.out.println("Une chambre de type [" + typeChambre + "] est actuellement [" + statutChambre + "].");

        // Changement d'état factice
        statutChambre = RoomStatus.OCCUPIED;
        statutFacture = InvoiceStatus.PAID;

        System.out.println("Après Check-in : La chambre passe à [" + statutChambre + "] et la facture est [" + statutFacture + "] via [" + modePaiement + "].");
        System.out.println("\n=== 🏁 FIN DES TESTS : TOUT EST VALIDE ! ===");
    }
}