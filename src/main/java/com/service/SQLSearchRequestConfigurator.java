package com.service;


/**
 * Класс формирующий поисковый запрос для базы данных SQL
 */
public class SQLSearchRequestConfigurator {
    /**
     * id паттерна в базе данных
     */
    private int patternGroup;
    /**
     * Название паттерна в базе данных
     */
    private String name;
    /**
     * Описание паттерна в базе данных
     */
    private String description;
    /**
     * Поисковый запрос к базе данных
     */
    private String searchRequest;

    /**
     * @param pattern модель для поиска нужного паттерна
     */
    public SQLSearchRequestConfigurator(PatternModel pattern){
        patternGroup = pattern.getGroup();
        name = pattern.getName();
        description = pattern.getDescription();
        searchRequest = createSearchRequest();
    }

    /**
     * Формирует основные параметры для поиска
     * @return готовый поисковый запрос
     */
    private String createSearchRequest(){
        String groupSearch = "";
        String nameSearch = "";
        String descriptionSearch = "";
        if (patternGroup != 0)
            groupSearch = "pattern_group ='"+patternGroup+"' ";
        if (name != null)
            nameSearch = "pattern_name like '%"+name+"%' ";
        if (description != null)
            descriptionSearch = "pattern_description like '%"+description+"%' ";
        return searchStatementWith3Parametres(groupSearch, nameSearch, descriptionSearch);
    }

    /**
     * Формирует поисковый запрос из трёх параметров поиска.
     * @param gropuSearch первый параметр поиска
     * @param nameSearch второй параметр поиска
     * @param descriptionSearch третий параметр поиска
     * @return готовый поисковый запрос
     */
    private String searchStatementWith3Parametres(String gropuSearch, String nameSearch, String descriptionSearch){
        if (gropuSearch.isEmpty() && nameSearch.isEmpty() && descriptionSearch.isEmpty())
            return "select * from patterns";
        if (gropuSearch.isEmpty() || nameSearch.isEmpty() || descriptionSearch.isEmpty()){
            if (gropuSearch.isEmpty() && !nameSearch.isEmpty() && !descriptionSearch.isEmpty())
                return searchStatementWith2Parametres(nameSearch, descriptionSearch);
            else if (nameSearch.isEmpty() && !gropuSearch.isEmpty() && !descriptionSearch.isEmpty())
                return searchStatementWith2Parametres(gropuSearch, descriptionSearch);
            else if (descriptionSearch.isEmpty() && !nameSearch.isEmpty() && !gropuSearch.isEmpty())
                return searchStatementWith2Parametres(gropuSearch, nameSearch);
            if (gropuSearch.isEmpty() && nameSearch.isEmpty() && !descriptionSearch.isEmpty())
                return searchStatementWith1Parametr(descriptionSearch);
            else if (gropuSearch.isEmpty() && descriptionSearch.isEmpty() && !nameSearch.isEmpty())
                return searchStatementWith1Parametr(nameSearch);
            else if (descriptionSearch.isEmpty() && nameSearch.isEmpty() && !gropuSearch.isEmpty())
                return searchStatementWith1Parametr(gropuSearch);
        }else return "select * from patterns where "+gropuSearch+" and "+descriptionSearch+" and "+nameSearch;
        return null;
    }

    /**
     * Формирует поисквый запрос из двух параметров поиска.
     * @param firstParametr первый парметр поиска
     * @param secondParametr второй параметр поиска
     * @return готовый поисковый запрос
     */
    private String searchStatementWith2Parametres(String firstParametr, String secondParametr){
        if (firstParametr.isEmpty() || secondParametr.isEmpty())
            if (firstParametr.isEmpty())
            return searchStatementWith1Parametr(secondParametr);
            else return searchStatementWith1Parametr(secondParametr);
        else return "select * from patterns where "+firstParametr+" and "+secondParametr;
    }

    /**
     * Формирует поисковый запрос из одного параметра
     * @param searchParametr парметр поиска
     * @return готовый поисковый запрос
     */
    private String searchStatementWith1Parametr(String searchParametr){
        return "select * from patterns where "+searchParametr;
    }

    /**
     * Возвращает поисковый запрос.
     * @return поисковый запрос
     */
    public String getSearchRequest(){
        return searchRequest;
    }
}
