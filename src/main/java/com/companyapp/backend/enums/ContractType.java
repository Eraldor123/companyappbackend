package com.companyapp.backend.enums;

/**
 * Enum reprezentující typy pracovních smluv, které mohou být přiřazeny uživatelům v systému.
 * - DPP (Dohoda o provedení práce): Tento typ smlouvy je určen pro krátkodobé a příležitostné práce, obvykle s omezením na 300 hodin ročně. Uživatelé s touto smlouvou mohou být přiřazeni k pracovnímu dni, ale jejich pracovní doba je omezena.
 * - HPP (Hlavní pracovní poměr): Tento typ smlouvy je určen pro dlouhodobé zaměstnání, kde uživatel pracuje na plný úvazek. Uživatelé s touto smlouvou mohou být přiřazeni k pracovnímu dni bez omezení pracovní doby.
 * - OSVC (Osoba samostatně výdělečně činná): Tento typ smlouvy je určen pro osoby, které pracují na volné noze nebo jako nezávislí dodavatelé. Uživatelé s touto smlouvou mohou být přiřazeni k pracovnímu dni, ale jejich pracovní doba není omezena a závisí na dohodě s plánovačem směn.
 */
public enum ContractType {
    DPP, HPP, OSVC, TERMINAL
}
