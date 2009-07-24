/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demo.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.demo.model.Song;
import org.hibernate.Session;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.jpa.FullTextEntityManager;

/**
 *
 * @author RanjiRam
 */
public class DemoHelper extends Observable {

    private static EntityManagerFactory entityManagerFactory;
    private static final String[] songFields = {"title", "artist", "album", "notes"};
    public static final String[] QUERY_TYPES = {"Multifield", "Wildcard", "Term", "Prefix", "Fuzzy", "Match-all"};
    public static final String[] QUERY_TYPES_DESC = {"Query all fields in one go<br>For example, if you want to retrieve songs that has title Ticket<br>or any field containing text LA,<br> you could use the following <i>title: \\\"Ticket\\\" \\\"LA\\\"</i>",
        "Queries with the help of two wildcard symbols '*' (multiplecharacters) and '?' (single character). <br>These allow to match any character.<br>Example, <i>b*ll</i> will match ball,bull or bell..",
        "It searches for a single term in a single field. Example, to retrieve all songs by Artist The Beatles, choose Artist field and search, <i>the beatles</i>",
        "A WildcardQuery that starts with characters and ends with the '*' symbol., Again, requires to choose a field to begin",
        "Queries using the Levenshtein distance between terms.<br>Requires a minimum similarity float value that expands or<br>contracts the distance.<br> For example, if you want to Search albums that sounds like builder, choose field Album and search for <i>builder</i> this will return 'builder and boulder'",
        "Returns all documents contained in a specified index."
    };
    static final int RECS_TO_LOAD = 13;
    private int percentageLoaded;
    private static final Log LOG = LogFactory.getLog(DemoHelper.class);

    private DemoHelper() {
        Ejb3Configuration configuration = new Ejb3Configuration();
        configuration.configure("music-db", new HashMap(1));
        entityManagerFactory = configuration.buildEntityManagerFactory();
    }

    public static DemoHelper getDemoHelper() {
        return HelperHolder.getDemoHelper();
    }

    public List<Song> getAllRecords() {
        LOG.info("Returning all records using Match all query");
        org.apache.lucene.search.Query luceneQuery = new MatchAllDocsQuery();
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Song> list = em.createQuery("select s from Song s").getResultList();

        //FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        //List<Song> list =  ftEm.createFullTextQuery(luceneQuery, Song.class).getResultList();
        return list;
    }

    public void insertData() {
        Thread loader = new Thread(new DataLoader());
        loader.start();
    }

    private void setPercentage(int percent) {
        percentageLoaded = percent;
        setChanged();
        notifyObservers();
    }

    public int getPercentage() {
        return percentageLoaded;
    }

    class DataLoader implements Runnable {

        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("data.txt")));

            EntityManager em = entityManagerFactory.createEntityManager();
            try {
                String line = null;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    count++;
                    String[] fields = line.split(",");
                    Song song = new Song();
                    song.setTitle(fields[0]);
                    song.setArtist(fields[1]);
                    song.setAlbum(fields[2]);
                    song.setNotes(fields[3]);
                    em.getTransaction().begin();
                    em.persist(song);
                    em.getTransaction().commit();
                    setPercentage((count * 100) / RECS_TO_LOAD);
                }
                buildIndex();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                em.close();
            }
        }
    }

    public List<Song> executeQuery(String query, String selectedFieldName, int queryType) throws ParseException {
        Query q = null;
        LOG.info("execute query begin");
        switch (queryType) {
            case 0://multi

                q = createHbMultiFieldSearchQuery(query);
                break;
            case 1://wildcard

                q = createHbWildCardQuery(selectedFieldName, query);
                break;
            case 2://term

                q = createHbTermQuery(selectedFieldName, query);
                break;
            case 3://prefix

                q = createHbPrefixQuery(selectedFieldName, query);
                break;
            case 4://fuzzy

                q = createHbFuzzyQuery(selectedFieldName, query);
                break;
            case 5://matchall

                q = createHbMatchAllQuery();

        }
        LOG.info("Query is of type " + q.getClass());
        return q.getResultList();
    }

    private Query createHbFuzzyQuery(String field, String userInput) {
        FuzzyQuery fq = new FuzzyQuery(new Term(field, userInput), 0.4F);
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(fq, Song.class);
    }

    private Query createHbMatchAllQuery() {
        org.apache.lucene.search.Query query = new MatchAllDocsQuery();
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(query, Song.class);
    }

    private Query createHbWildCardQuery(String field, String wildCard) {
        org.apache.lucene.search.Query query = new WildcardQuery(new Term(field, wildCard));
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(query, Song.class);
    }

    private Query createHbTermQuery(String searchField, String searchText) {
        String[] toks = searchText.split("\\s");
        BooleanQuery booleanQuery = new BooleanQuery();
        for (String tok : toks) {
            org.apache.lucene.search.Query termQuery = new TermQuery(new Term(searchField, tok));
            booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        }

        EntityManager manager = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftem = org.hibernate.search.jpa.Search.getFullTextEntityManager(manager);
        return ftem.createFullTextQuery(booleanQuery, Song.class);
    }

    private Query createHbPrefixQuery(String field, String userInput) {
        org.apache.lucene.search.Query query = new PrefixQuery(new Term(field, userInput));
        EntityManager em = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        return ftEm.createFullTextQuery(query, Song.class);
    }

    private Query createHbMultiFieldSearchQuery(String searchText) throws ParseException {
        Map<String, Float> weightMap = new HashMap<String, Float>(1);
        /*weightMap.put("title", 5F);
        weightMap.put("artist", 5F);
        weightMap.put("album", 5F);
        weightMap.put("notes", 5F);*/
        QueryParser queryParser = new MultiFieldQueryParser(songFields, new StandardAnalyzer());
        queryParser.setDefaultOperator(QueryParser.OR_OPERATOR);
        org.apache.lucene.search.Query lQuery = queryParser.parse(searchText);
        EntityManager manager = entityManagerFactory.createEntityManager();
        FullTextEntityManager ftem = org.hibernate.search.jpa.Search.getFullTextEntityManager(manager);
        return ftem.createFullTextQuery(lQuery, Song.class);

    }

    private void buildIndex() {
        EntityManager manager = entityManagerFactory.createEntityManager();
        try {
            manager.getTransaction().begin();
            //Hibernate search in action. All classes below are from hibernate
            FullTextSession fullTextSession = Search.getFullTextSession((Session) manager.getDelegate());
            List songObjList = fullTextSession.createCriteria(Song.class).list();
            LOG.info("Building index");
            //manually building our index
            for (Object songObject : songObjList) {
                fullTextSession.index(songObject);
            }
        } finally {
            manager.getTransaction().commit();
            manager.close();
            LOG.info("Built index");
        }
    }

    /**
     * Singleton holder
     */
    private static class HelperHolder {
        private static final DemoHelper HELPER = new DemoHelper();

        static DemoHelper getDemoHelper() {
            return HELPER;
        }
    }
}
