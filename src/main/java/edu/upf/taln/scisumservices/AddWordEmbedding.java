package edu.upf.taln.scisumservices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.sqlite.SQLiteConfig;

import com.mysql.jdbc.Statement;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.util.InvalidOffsetException;

public class AddWordEmbedding extends AbstractLanguageAnalyser  implements
ProcessingResource, Serializable {
	
	
	HashMap<String, INDArray> cache;
	HashSet<String> stopwords;
	
	private boolean dockerDomain;
    
    public boolean isDockerDomain() {
		return dockerDomain;
	}
	public void setDockerDomain(boolean dockerDomain) {
		this.dockerDomain = dockerDomain;
	}
	public HashSet<String> getStopwords() {
		return stopwords;
	}
	public void setStopwords(HashSet<String> stopwords) {
		this.stopwords = stopwords;
	}
	// the document to process
    Document document;
    public Document getDocument() {
        return document;
    }
    public void setDocument(Document d) {
        document=d;
    }
    // the input annotation set
    String sentenceAnnSet;
    public String getSentenceAnnSet() {
        return sentenceAnnSet;
    }
    public void setSentenceAnnSet(String as) {
        sentenceAnnSet=as;
    }
    
    // sentence 
    
    String sentAnn;
    public String getSentAnn() {
        return sentAnn;
    }
    public void setSentAnn(String sa) {
        sentAnn=sa;
    }
    
    int nTokensDoc;
    
    INDArray centroidDoc;
    
    public INDArray getCentroidDoc() {
    	
    	if (centroidDoc != null) {
    		centroidDoc = centroidDoc.div(nTokensDoc);
        }else {
        	
        	float arrayVector[] = new float[300];           
            for (int i = 0; i < 300; i++) {
                arrayVector[i] = (float)0.0;
            }    
            centroidDoc = new NDArray(arrayVector);
        }
    	
		return centroidDoc;
	}
	public void setCentroidDoc(INDArray centroidDoc) {
		this.centroidDoc = centroidDoc;
	}
	String tokenAnn;
    
    
	public String getTokenAnn() {
		return tokenAnn;
	}
	public void setTokenAnn(String tokenAnn) {
		this.tokenAnn = tokenAnn;
	}
	
	
	String vectorAnn = "";
	
	public String getVectorAnn() {
		return vectorAnn;
	}
	public void setVectorAnn(String vectorAnn) {
		this.vectorAnn = vectorAnn;
	}
	
	public String tableName = "";

	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tn) {
		this.tableName = tn;
	}

	Connection connection;
	
	String vecFeatName;
    
    
    public String getVecFeatName() {
		return vecFeatName;
	}
	public void setVecFeatName(String vecFeatName) {
		this.vecFeatName = vecFeatName;
	}

	String docID;
    public String getDocID() {
        return docID;
    }
    public void setDocID(String sa) {
    	docID=sa;
    }
	
	public Resource init() {
		nTokensDoc = 0;
		centroidDoc = null;
		//String dbHost = "scipub-taln.s.upf.edu";
	    //String dbPort = "3306";
	    //String dbDatabase = "embeddings";
	    //String dbUsername = "taln_test";
	    //String dbPassword = "test";
	    cache = new HashMap<String, INDArray>();
	    String url = "/home/upf/eclipse-workspace/scisumservices/embeddings_sqlite3.db";
	    //url = "/home/upf/eclipse-workspace/docker_ubu1604_jdk8_mvn_sqlitedata/embeddings_sqlite3.db";
	    if (dockerDomain)
	    	url = "/scisumservices/embeddings_sqlite3.db";
	    //connection = getDatabaseConnectionPool(dbHost, dbPort, dbDatabase, dbUsername, dbPassword);
	    
	    connection = getDatabaseConnectionPoolSqlite(url);
        
	    //getVectorLite("car");
	    //getVectorLite("stop");
	    //getVectorLite("medical");
	    
	    //System.exit(3);
	    
	    return this;
    }
	/*
	public static Connection getDatabaseConnectionPool(String dbHost, String dbPort, String dbDatabase, String dbUsername, String dbPassword) {
		Connection conn = null;
		
		
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection("jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbDatabase, dbUsername, dbPassword);
		    
		   
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return conn;
		
	}*/
	public Connection getDatabaseConnectionPoolSqlite(String url) {
		Connection conn = null;
		
		try {
			
			SQLiteConfig config = new SQLiteConfig();
			config.setReadOnly(true);
			
			Class.forName("org.sqlite.JDBC");
		    conn = DriverManager.getConnection("jdbc:sqlite:"+url, config.toProperties());
		   
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return conn;
		
	}
    
	
	/*
    public static INDArray getVector(String word) {
        INDArray vector = null;
        word = word.replace("'","\\\'");//.replace("\"","\\\"");
        String  sql = "SELECT * FROM "+tableName+" WHERE word='"+ word +"'";
        try (Statement statement = (Statement) connection.createStatement()) {
        	
        	
        	
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int numCols = metaData.getColumnCount();
            float arrayVector[] = new float[numCols-1];           
            if (resultSet.next()) {
                for (int i = 0; i < numCols-1; i++) {
                    int colNumber = i+2;
                    arrayVector[i] = resultSet.getFloat(colNumber);
                    System.out.println("" + arrayVector[i]);
                }    
                vector = new NDArray(arrayVector);
            }                               
        } catch (SQLException e) {
            System.out.println("Error while retrieving vector embedding: '" + word +"'");
            //System.out.println(e.toString());
            //e.printStackTrace();
        } 
        return vector;
    }*/
    
    public INDArray getVectorLite(String word) {
        INDArray vector = null;
        word = word.replace("'","\\\'");//.replace("\"","\\\"");
        String  sql = "SELECT * FROM "+tableName+" WHERE word='"+ word +"'";
        
        
        
        if (cache.containsKey(word))
        	return cache.get(word);
        
        sql = "SELECT * FROM "+tableName+" WHERE word = ?";
        //sql = "SELECT * FROM google_en_300 WHERE word = ?";
        try  {
        	//System.out.println(sql);
        	PreparedStatement statement = connection.prepareStatement(sql);
        	
        	statement.setString(1, word);
        	
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int numCols = metaData.getColumnCount();
            float arrayVector[] = new float[numCols-1];           
            if (resultSet.next()) {
                for (int i = 0; i < numCols-1; i++) {
                    int colNumber = i+2;
                    arrayVector[i] = resultSet.getFloat(colNumber);
                }    
                vector = new NDArray(arrayVector);
            }                               
        } catch (SQLException e) {
            System.out.println("Error while retrieving vector embedding: '" + word +"'");
            System.out.println(e.toString());
            e.printStackTrace();
        } 
        
        cache.put(word, vector);
        
        return vector;
    }
    
    
    
    
    private INDArray extractTokenInfo(List<Annotation> speTokenSet) throws InvalidOffsetException, IOException {
    	
    	INDArray centroid = null;
    	
    	int ntokens = 0;
    	for (Annotation token: speTokenSet) {
    		
    		if (!token.getFeatures().get("kind").toString().equals("word"))
    			continue;
    		
    		FeatureMap feat = token.getFeatures();
    		
    		String tokenText = feat.get("string").toString();
    		
    		String word = tokenText.toLowerCase();
    		
    		
    		
			INDArray vector = getVectorLite(word);
			
			if (vector== null)
				continue;
			
			ntokens++;
			nTokensDoc++;
            if (centroid != null) {
                if (vector != null) {
                    centroid = centroid.add(vector);
                }
            }   
            else if (vector != null) {
                centroid = vector;
            }
            
            
            if (centroidDoc != null) {
                if (vector != null) {
                	centroidDoc = centroidDoc.add(vector);
                }
            }   
            else if (vector != null) {
            	centroidDoc = vector;
            }
    		
    	}
    	
    	if (centroid != null) {
            centroid = centroid.div(ntokens);
        }else {
        	
        	float arrayVector[] = new float[300];           
            for (int i = 0; i < 300; i++) {
                arrayVector[i] = (float)0.0;
            }    
            centroid = new NDArray(arrayVector);
        }
    	
    	return centroid;
    	
    	
    }
    


public void execute() {
    Document doc=getDocument();
    
    AnnotationSet all;
    List<Annotation> sentences;
    AnnotationSet tokensAnnSet;
    
    try {
    	
    	if(sentenceAnnSet.equals("")) {
            all=doc.getAnnotations();
        } else {
            all=doc.getAnnotations(sentenceAnnSet);
        }
        
        sentences=all.get(sentAnn).inDocumentOrder();
        
        tokensAnnSet = all.get(tokenAnn);
        
    	for (int i=0; i<sentences.size(); i++){
    		// Token
    		Annotation sent = sentences.get(i);
    		
    		//FeatureMap feat = sent.getFeatures();
    		
    		List<Annotation> speTokenSet = tokensAnnSet.get(sent.getStartNode().getOffset(), sent.getEndNode().getOffset()).inDocumentOrder();
    		
    		INDArray centroid = extractTokenInfo(speTokenSet);
    		
    		FeatureMap featNew = Factory.newFeatureMap();
    		
			for (int j =0; j < centroid.columns();j++) {
				String dimension = "000" + j;
				dimension = "d_" + dimension.substring(dimension.length() - 3);
				featNew.put(dimension, "" + centroid.getFloat(j));
			}
			
			all.add(sent.getStartNode().getOffset(), sent.getEndNode().getOffset(), vectorAnn, featNew);
    		
    	}
    	
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InvalidOffsetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   
    
}
	
}
