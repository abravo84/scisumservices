package edu.upf.taln.scisumservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.util.ModuleConfig;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import summa.SimpleSummarizer;
import summa.analyser.NormalizeVector;
import summa.resources.frequency.InvertedTable;
import summa.resources.frequency.NEFrequency;
import summa.resources.frequency.VectorComputation;
import summa.scorer.PositionScorer;
import summa.scorer.SentenceTermFrequency;
import summa.scorer.TextRankScore;
import summa.summarizer.ExportSelectedSentences;
import summa.scorer.ParagraphScorer;
import summa.scorer.TitleSentenceSim;
import summa.scorer.FirstSentenceSimilarity;
import summa.scorer.QuerySentenceSim;
import summa.scorer.SentenceDocumentSimilarity;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.constants.OperationType;

@Component(OperationType.ANALYZER)
public class SciSumService extends AbstractLanguageAnalyser  implements
ProcessingResource, Serializable{
	
	private static boolean DRIProcessing = true;
	private static boolean SUMMAProcessing = true;
	private static boolean dockerDomain = false;
	
	static NEFrequency NEstatistics;

	static InvertedTable enTable;

	public static ProcessingResource enVectorPR;
	public static ProcessingResource normVecPR;
	public static ProcessingResource docVectorPR;
	public static ProcessingResource docNormVecPR;
	
	//Scorers
	static SentenceTermFrequency sentTermFreq;
	static PositionScorer posScorer;
	static TitleSentenceSim sentenceSim;
	static FirstSentenceSimilarity firstSentenceSimilarity;
	static QuerySentenceSim querySentenceSim;
	static ParagraphScorer paragraphScorer;
	static SentenceDocumentSimilarity docSim;
	
	static TextRankScore textRankScorer;
	
	static SimpleSummarizer simpleSumm;
	static ExportSelectedSentences exporter;
	
	static AddWordEmbedding addw2VGoogle;
	static AddWordEmbedding addw2VWiki;
	static AddWordEmbedding addw2VBio;
	
	static FeaturesManagement featManagement;
	
	public String outputPath;
	
	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String inputPath;
	
	public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public static void saveOutputFile(String text, File f) throws IOException {
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		bw.write(text);
		bw.close();
	}
	
	/**
     * Complete missing lemmas - From ComputeSUMMAVecs, by Horacio.
     * @param gateDoc
     */
    public static void completeLemmas(Document gateDoc) {
        AnnotationSet gateTokens = gateDoc.getAnnotations("Analysis").get("Token");
        Iterator<Annotation> tokensIterator = gateTokens.iterator();
        FeatureMap tokenFeatures;
        Annotation tokenAnnotation;
        while (tokensIterator.hasNext()) {
            tokenAnnotation = tokensIterator.next();
            tokenFeatures = tokenAnnotation.getFeatures();
            if (!tokenFeatures.containsKey("lemma")) {
                if (tokenFeatures.containsKey("string")) {
                    tokenFeatures.put("lemma", ((String) tokenFeatures.get("string")).toLowerCase());
                } else {
                    tokenFeatures.put("lemma", gateDoc.getContent().toString().substring(
                            tokenAnnotation.getStartNode().getOffset().intValue(),
                            tokenAnnotation.getEndNode().getOffset().intValue()
                    ).toLowerCase());
                }
            }
        }
    }    
    
    
    
	public static void initSUMMA() {
		
		try {
            
			
		String resourcePath = Gate.getGateHome().getAbsoluteFile() + "/plugins/summa_plugin";
		
		System.out.println("Resource Path: " + resourcePath);

		String baseSUMMApath = "file:///" + resourcePath + File.separator + "resources" + File.separator;
		
		enTable=new InvertedTable();
		enTable.setParameterValue("encoding", "UTF-8");
		enTable.setParameterValue("tableLocation", 
				new URL(baseSUMMApath + "aquaint.idf"));
		enTable.init();

		NEstatistics=new NEFrequency();
		NEstatistics.setParameterValue("table",enTable);
		NEstatistics.setParameterValue("annSet","Analysis");
		NEstatistics.setParameterValue("annType","Token");
		NEstatistics.setParameterValue("featureName","lemma");
		NEstatistics.setParameterValue("sentAnn","Sentence");
		NEstatistics.setParameterValue("parAnn","para");
		NEstatistics.setParameterValue("paraStat","para");
		NEstatistics.setParameterValue("sentStat","sent");
		NEstatistics.setParameterValue("tokenStat","token");
		NEstatistics.setParameterValue("kindF","kind");
		NEstatistics.setParameterValue("kindV","word");
		NEstatistics.init();

		/* English */
		URL stopTableEn=new URL(baseSUMMApath + "en_stop_words.lst");
		URL stopKindEn=new URL(baseSUMMApath + "stop_kind.lst");

		// English
		enVectorPR=new VectorComputation();
		enVectorPR.setParameterValue("initVectors",Boolean.TRUE);
		enVectorPR.setParameterValue("vecAnn","Vector");
		enVectorPR.setParameterValue("tokenAnn", "Token");
		enVectorPR.setParameterValue("tokenFeature", "lemma");
		enVectorPR.setParameterValue("sentAnn","Sentence");
		enVectorPR.setParameterValue("statistics", "sent_tf_idf");
		enVectorPR.setParameterValue("encoding","UTF-8");
		enVectorPR.setParameterValue("stopTag","kind");
		enVectorPR.setParameterValue("stopFeature", "string");
		enVectorPR.setParameterValue("lowercase",Boolean.TRUE);
		enVectorPR.setParameterValue("stopTagLoc", stopKindEn);
		enVectorPR.setParameterValue("stopWordLoc", stopTableEn);
		enVectorPR.setParameterValue("annSetName","Analysis");
		enVectorPR.init();


		normVecPR=new NormalizeVector();
		normVecPR.setParameterValue("annSet", "Analysis");
		normVecPR.setParameterValue("vecAnn", "Vector");
		normVecPR.init();
		
		
		//Vector for Document
		docVectorPR=new VectorComputation();
		docVectorPR.setParameterValue("initVectors",Boolean.TRUE);
		docVectorPR.setParameterValue("vecAnn","DocVector");
		docVectorPR.setParameterValue("tokenAnn", "Token");
		docVectorPR.setParameterValue("tokenFeature", "lemma");
		docVectorPR.setParameterValue("sentAnn","Document");
		docVectorPR.setParameterValue("statistics", "token_tf_idf");
		docVectorPR.setParameterValue("encoding","UTF-8");
		docVectorPR.setParameterValue("stopTag","kind");
		docVectorPR.setParameterValue("stopFeature", "string");
		docVectorPR.setParameterValue("lowercase",Boolean.TRUE);
		docVectorPR.setParameterValue("stopTagLoc", stopKindEn);
		docVectorPR.setParameterValue("stopWordLoc", stopTableEn);
        docVectorPR.setParameterValue("annSetName","Analysis");
        docVectorPR.init();
		
        docNormVecPR=new NormalizeVector();
        docNormVecPR.setParameterValue("annSet", "Analysis");
        docNormVecPR.setParameterValue("vecAnn", "DocVector");
        docNormVecPR.init();
        
        
        docSim = new SentenceDocumentSimilarity();
        docSim.setSentAnnSet("Analysis");
        docSim.setSentAnn("Sentence");
        docSim.setDocVecName("DocVector_Norm");
        docSim.setVecAnn("Vector_Norm");
        docSim.setFname("Doc_Sim");
        docSim.init();
		
		sentTermFreq = new SentenceTermFrequency();
		sentTermFreq.setParameterValue("annSetName", "Analysis");
		sentTermFreq.setParameterValue("sentAnn", "Sentence");
		sentTermFreq.setParameterValue("statFeature", "token_tf_idf");
		sentTermFreq.setParameterValue("termFreqFeature", "tf_score");
		sentTermFreq.setParameterValue("wordAnn", "Token");
		sentTermFreq.init();
		
		posScorer = new PositionScorer();
		posScorer.setParameterValue("annSetName", "Analysis");
		posScorer.setParameterValue("sentAnn", "Sentence");
		posScorer.setScoreName("position_score");
		posScorer.init();
		
		sentenceSim = new TitleSentenceSim();
		sentenceSim.setParameterValue("annSet", "Analysis");
		sentenceSim.setParameterValue("sentAnn", "Sentence");
		
		sentenceSim.setParameterValue("titleAnnSet", "TITLE");
		sentenceSim.setParameterValue("titleFeature", "title_sim");
		sentenceSim.setParameterValue("vector", "Vector_Norm");
		
		sentenceSim.init();
		
		
		
		
		addw2VWiki = new AddWordEmbedding();
		addw2VWiki.setSentenceAnnSet("Analysis");
		addw2VWiki.setSentAnn("Sentence");
		addw2VWiki.setTokenAnn("Token");
		addw2VWiki.setTableName("wiki_en_300");
		addw2VWiki.setVectorAnn("WikiSentVector");
		addw2VWiki.setDockerDomain(dockerDomain);
		addw2VWiki.init();
    	
    	
    	addw2VGoogle = new AddWordEmbedding();
    	addw2VGoogle.setSentenceAnnSet("Analysis");
    	addw2VGoogle.setSentAnn("Sentence");
    	addw2VGoogle.setTokenAnn("Token");
    	addw2VGoogle.setTableName("google_en_300");
    	addw2VGoogle.setVectorAnn("GoogleSentVector");
    	addw2VGoogle.setDockerDomain(dockerDomain);
    	addw2VGoogle.init();
    	
    	addw2VBio = new AddWordEmbedding();
    	addw2VBio.setSentenceAnnSet("Analysis");
    	addw2VBio.setSentAnn("Sentence");
    	addw2VBio.setTokenAnn("Token");
    	addw2VBio.setTableName("pubmed_pmc_en_300");
    	addw2VBio.setVectorAnn("BioSentVector");
    	addw2VBio.setDockerDomain(dockerDomain);
    	addw2VBio.init();
    	
    	
    	
    	featManagement = new FeaturesManagement();
    	featManagement.setSentenceAnnSet("Analysis");
    	featManagement.setSentAnn("Sentence");
    	featManagement.init();
    	
		/*
		simpleSumm = new SimpleSummarizer();
		//simpleSumm.setParameterValue("sentAnn", "Sentence");
		simpleSumm.init();
		
		exporter =  new ExportSelectedSentences();
		exporter.init();
		*/
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void executeTextRank(Document gateDoc, String featName, String vecName) {
		try {
			
			textRankScorer = new TextRankScore();
			textRankScorer.setComparatorClass(summa.scorer.TextRankScore.CosineComparator);
			textRankScorer.setParameterValue("fname", featName);
		
			textRankScorer.setParameterValue("sentAnn", "Sentence");
			textRankScorer.setParameterValue("sentAnnSet", "Analysis");
			textRankScorer.setParameterValue("vecAnn", vecName);
			textRankScorer.setParameterValue("smooth", 0.85);
			
			textRankScorer.init();
			
			textRankScorer.setParameterValue("document", gateDoc);
			textRankScorer.execute();
		
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	 private static void addVectorsDocument(Document gateDoc) throws ResourceInstantiationException, ExecutionException {
		 try {
			 	completeLemmas(gateDoc);
		    	
		    	addw2VBio.setDocument(gateDoc);
		    	addw2VBio.setTableName("pubmed_pmc_en_300");
		    	addw2VBio.execute();
		    	//INDArray bioDocVector = addw2VWiki.getCentroidDoc();
		    	
		    	addw2VGoogle.setDocument(gateDoc);
		    	addw2VGoogle.setTableName("google_en_300");
		    	addw2VGoogle.execute();
		    	//INDArray googleDocVector = addw2VWiki.getCentroidDoc();
		    	
		    	addw2VWiki.setDocument(gateDoc);
		    	addw2VWiki.setTableName("wiki_en_300");
		    	addw2VWiki.execute();
		    	//INDArray wikiDocVector = addw2VWiki.getCentroidDoc();
		    	
		    	
				NEstatistics.setParameterValue("document", gateDoc);
				NEstatistics.execute();
				
				enVectorPR.setParameterValue("document", gateDoc);
				enVectorPR.execute();
					
		        normVecPR.setParameterValue("document", gateDoc);
		        normVecPR.execute();  
		        
		        sentTermFreq.setParameterValue("document", gateDoc);
		        sentTermFreq.execute(); 
		        
		        posScorer.setParameterValue("document", gateDoc);
		        posScorer.execute();
		        
		        
		        executeTextRank(gateDoc, "TextRank_Freq", "Vector");
		        executeTextRank(gateDoc, "TextRank_Bio", "BioSentVector");
		        executeTextRank(gateDoc, "textRank_Google", "GoogleSentVector");
		        executeTextRank(gateDoc, "textRank_Wiki", "WikiSentVector");
		        
		        
		        featManagement.setParameterValue("document",gateDoc);
		        featManagement.execute();
		        
		        
		        AnnotationSet analysis=gateDoc.getAnnotations("Analysis");
	            Long startAnn, endAnn;
	            startAnn=analysis.firstNode().getOffset();
	            endAnn=analysis.lastNode().getOffset();
            
				analysis.add(startAnn, endAnn, "Document", Factory.newFeatureMap());
				
				
				docVectorPR.setParameterValue("document",gateDoc);
		        docVectorPR.execute();
		        
		        docNormVecPR.setParameterValue("document",gateDoc);
		        docNormVecPR.execute();
				
		        docSim.setDocument(gateDoc);
		        docSim.execute();
				
			} catch (InvalidOffsetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    } 
	
	 public Resource init() {
			if (DRIProcessing)
				initDRI();
			else
				initGATE();
			if (SUMMAProcessing)
				initSUMMA();
			return this;
		}
	 
	public static void initDRI() {
		String basePath = new File("").getAbsolutePath();
		// 1) Set the full path to the Dr. Inventor Framework Property files
		
		String DRIPropPath = basePath + "/src/main/resources/drinventor/DRIconfig4.0.properties";
		
		if (dockerDomain)
			DRIPropPath = "/scisumservices/DRIconfig4.0.properties";
		System.out.println("Resource File: " + DRIPropPath);
		
		
		edu.upf.taln.dri.lib.Factory.setDRIPropertyFilePath(DRIPropPath);
		
		
		
		// 2) Programmatically configure the PDF processing options (http://driframework.readthedocs.io/en/latest/Initialize/)
		//Factory.setPDFtoTextConverter(PDFtoTextConvMethod.GROBID);

		
		// 3) Choose the set of scientific text mining modules to use when analyzing scientific (http://driframework.readthedocs.io/en/latest/Initialize/)
		//      3.1) Instantiate the ModuleConfig class - the constructor sets all modules enabled by default
		ModuleConfig modConfigurationObj = new ModuleConfig();

		//      3.2.A) Enable the parsing of bibliographic entries by means of online services (Bibsonomy, CrossRef, FreeCite, etc.)
		modConfigurationObj.setEnableBibEntryParsing(false); // Set to false in order no to parse bibliographic entries

		//      3.2.B) Enable BabelNet Word Sense Disambiguation and Entity Linking over the text of the paper
		//      You should provide your BabelNet API key in the Dr. Inventor Framework Property files to use this feature
		modConfigurationObj.setEnableBabelNetParsing(false);

		//      3.2.C) Enable the parsing of the information from the header of the paper by means of online services (Bibsonomy, CrossRef, FreeCite, etc.)
		modConfigurationObj.setEnableHeaderParsing(false);

		//      3.2.D) Enable the extraction of candidate terms from the sentences of the paper
		modConfigurationObj.setEnableTerminologyParsing(false);

		//      3.2.E) Enable the dependency parsing of the sentences of a paper
		modConfigurationObj.setEnableGraphParsing(true);

		//      3.2.F) Enable coreference resolution
		modConfigurationObj.setEnableCoreferenceResolution(false);

		//      3.2.G) Enable the extraction of causal relations
		modConfigurationObj.setEnableCausalityParsing(false);

		//      3.2.H) Enable the association of a rhetorical category to the sentences of the paper
		modConfigurationObj.setEnableRhetoricalClassification(true);

		// 3.3) Improt the configuration parameters set in the ModuleConfig instance
		edu.upf.taln.dri.lib.Factory.setModuleConfig(modConfigurationObj); 
		
		// 4) Initialize the library - pre-load the resources needed to process scientific publications
		try {
			edu.upf.taln.dri.lib.Factory.initFramework();
		} catch (DRIexception e) {
			System.out.println("Error while initializing the Dr. Inventor Text Mining Framework!");
			e.printStackTrace();
		}
	}
	
	public static void initGATE() {
		try {
			Gate.init();
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static Document convertXML2Gate(String gateDocPath) {
		
		Document gateDoc = null;
		
		try {
			gateDoc = Factory.newDocument(new URL("file:///" + gateDocPath), "utf-8");
			
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return gateDoc;
	}
	
	public static Document driProcessingStep(String gateDocPath, boolean writeDoc, String gatefilePath) {
			
			
		Document gateDoc = null;
		try {
			gateDoc = Factory.newDocument(new URL("file:///" + gateDocPath), "utf-8");
		
		
		    String tmpGateFilePath = gateDocPath.replace(".nxml", "_GATE.xml");
		    File gateFile = new File(tmpGateFilePath);                          
		    saveOutputFile(gateDoc.toXml(), gateFile);
		    edu.upf.taln.dri.lib.model.Document driGateDocument = edu.upf.taln.dri.lib.Factory.createNewDocument(gateFile);
	        driGateDocument.preprocess();
	        if (writeDoc)
	        	saveOutputFile(driGateDocument.getXMLString(), new File(gatefilePath));
	        
			
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InternalProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DRIexception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gateDoc;
		
	}
	
	public static Document driProcessingStep(String gateDocPath) {
		
		
		Document gateDoc = null;
		try {
			gateDoc = Factory.newDocument(new URL("file:///" + gateDocPath), "utf-8");
		
		
		    String tmpGateFilePath = gateDocPath.replace(".nxml", "_GATE.xml");
		    File gateFile = new File(tmpGateFilePath);                          
		    saveOutputFile(gateDoc.toXml(), gateFile);
		    edu.upf.taln.dri.lib.model.Document driGateDocument = edu.upf.taln.dri.lib.Factory.createNewDocument(gateFile);
	        driGateDocument.preprocess();
	        
			
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InternalProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DRIexception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gateDoc;
		
	}
	
	
	
	public static Document driProcessingStep(Document gateDoc) {
		
		edu.upf.taln.dri.lib.model.Document driGateDocument =  null;
		try {
		    String tmpGateFilePath = System.getProperty("java.io.tmpdir") + File.separator + "GATE.xml";
		    if (dockerDomain)
		    	tmpGateFilePath = "/scisumservices/" +gateDoc.getName() + "_GATE.xml";
		    File gateFile = new File(tmpGateFilePath);                          
		    saveOutputFile(gateDoc.toXml(), gateFile);
		    driGateDocument = edu.upf.taln.dri.lib.Factory.createNewDocument(gateFile);
	        driGateDocument.preprocess();
	        gateFile.delete();
	        gateDoc = Factory.newDocument(driGateDocument.getXMLString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InternalProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DRIexception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return gateDoc;
		
	}
	
	public static void summaProcessingStep(String driDocPath, boolean writeDoc, String summaDocPath) {
		try {
	        
	        Document gateDocDRI = Factory.newDocument(new URL("file:///" + driDocPath), "utf-8");
	        
	        addVectorsDocument(gateDocDRI);
	        
	        
	        
	        
	        if (writeDoc)
	        	saveOutputFile(gateDocDRI.toXml(), new File(summaDocPath));
	        
			Factory.deleteResource(gateDocDRI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void summaProcessingStep(Document gateDocDRI, boolean writeDoc, String summaDocPath) {
		try {
	        
	        addVectorsDocument(gateDocDRI);
	        
	        if (writeDoc)
	        	saveOutputFile(gateDocDRI.toXml(), new File(summaDocPath));
	        
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static Document summaProcessingStep(Document gateDocDRI) {
		try {
	        
	        addVectorsDocument(gateDocDRI);
			
		}  catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return gateDocDRI;
	}
	
	
	
	public void execute() {
		
		Document gateDoc = convertXML2Gate(inputPath);
		
		//Document gateDoc=getDocument();
		if (DRIProcessing)
			gateDoc = driProcessingStep(gateDoc);
		
		// SUMMA
        if (SUMMAProcessing && gateDoc != null) {
        	gateDoc = summaProcessingStep(gateDoc);
        
        }
        
        
        try {
			saveOutputFile(gateDoc.toXml(), new File(outputPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Factory.deleteResource(gateDoc);
	}
	
	
	
	public static void main( String[] args )
    {
		
		
		//URL[] descriptorPaths = DescriptorFactory.scanDescriptors();
		
		String inputFolder = args[0];
        File inDir=new File(inputFolder);
        File[] flist=inDir.listFiles();
        
        String outputPath = args[1];
        
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }
        
        
        SciSumService textpro = new SciSumService();
        
        
        Arrays.sort(flist);
        textpro.init();
        
        for (File f: flist) {
			String xmlPath = f.getAbsolutePath();
			String xmlFileName = f.getName();
			
			textpro.setInputPath(xmlPath);
			textpro.setOutputPath(outputPath + File.separator + xmlFileName);
			//textpro.setDocument(gateDoc);
			textpro.execute();
			
			
        }
    }
}
