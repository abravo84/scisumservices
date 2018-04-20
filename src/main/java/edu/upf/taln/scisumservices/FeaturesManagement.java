package edu.upf.taln.scisumservices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.util.InvalidOffsetException;





public class FeaturesManagement extends AbstractLanguageAnalyser implements
ProcessingResource, Serializable {
	
	
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
	
	public Resource init() {
		
		
		return this;
	}
	
	public void execute() {
		
		Document doc=getDocument();
	    
	    AnnotationSet all;
	    List<Annotation> sentences;
	    
	    if(sentenceAnnSet.equals("")) {
		    all=doc.getAnnotations();
		} else {
		    all=doc.getAnnotations(sentenceAnnSet);
		}
		
		sentences=all.get(sentAnn).inDocumentOrder();
		
		
		
		int totalCUES = 0;
		
		
		for (int i=0; i<sentences.size(); i++){
			
			int specificCUES = 0;
			//float simTitle = (float) 0.0;
			
			Annotation sent = sentences.get(i);
			
			FeatureMap feat = sent.getFeatures();
			
			Set<Object>featKey = feat.keySet();
			
			
			for (Object feat_name:featKey) {
				
				String kName = (String)feat_name;
				
				if (kName.startsWith("FEAT")) {
					
					if (kName.endsWith("TEUFEL")) {
						int value = (int)Float.parseFloat((String)feat.get(feat_name));
						totalCUES += value;
						specificCUES += value;
					}
					else if (kName.equals("FEAT_SIM_TITLE")) {
						//simTitle = (float)feat.get(feat_name);
						feat.put("SimTitle", feat.get(feat_name));
					}
					
					feat.remove(feat_name);
					
				}else if (kName.startsWith("rhetorical"))
					feat.remove(feat_name);
				else if (kName.startsWith("JATS"))
					feat.remove(feat_name);
				else if (kName.startsWith("sent_tf_idf"))
					feat.remove(feat_name);
				
			}
			feat.put("CuePhrase", ""+specificCUES);
			
			
		}
		
		for (int i=0; i<sentences.size(); i++){
			//float simTitle = (float) 0.0;
			Annotation sent = sentences.get(i);
			FeatureMap feat = sent.getFeatures();
			int cue_count = Integer.parseInt((String)feat.get("CuePhrase"));
			float div = (float)cue_count/totalCUES;
			feat.put("CuePhrase_Avg",""+ div);
		}
		
		
	    
	}

}
