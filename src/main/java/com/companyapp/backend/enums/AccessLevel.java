package com.companyapp.backend.enums;

/**
 * Enum reprezentující různé úrovně přístupu pro uživatele v systému.
 * - ADMIN: Nejvyšší úroveň přístupu, umožňuje plnou kontrolu nad systémem, včetně správy uživatelů, nastavení a všech funkcí.
 * - MANAGEMENT: Uživatelé s touto úrovní mají přístup k většině funkcí, ale nemohou spravovat uživatele ani měnit systémová nastavení.
 * - PLANNER: Uživatelé s touto úrovní mají přístup k plánování směn a správě směn, ale nemohou měnit systémová nastavení ani spravovat uživatele.
 * - BASIC: Nejnižší úroveň přístupu, umožňuje pouze zobrazení informací o směnách a vlastních přiřazeních, bez možnosti úprav.
 */
public enum AccessLevel {
    ADMIN, MANAGEMENT, PLANNER, BASIC, TERMINAL;;
}
