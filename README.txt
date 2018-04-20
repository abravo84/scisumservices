OpenMinTeD Scientific Summarization Services 1.0.0
---------------------------------------------------

Scientific Summarization Services is a system available on the OpenMinTeD platform in order to automatically identify the most important information of a research article. The system integrates multiple NLP tools such as Dr. Inventor (DRI) Text Mining Framework, SUMMA and Word Embeddings.

Briefly, from a scientific article the system analyzes, extracts and characterizes several aspects, such as structural elements of the article and the discursive category and vector representations of each sentence. This information is used to compute different scores to rank each sentence of the article. The most relevant sentences will have a higher score. The system has been shared as a Docker component and the code released in Github with a Creative Commons Attribution 4.0 license, can be found in the following links:

	--> https://github.com/abravo84/scisumservices

	--> https://hub.docker.com/r/abravp/openminted_scisumservices:1.0.0

The GitHub Repository does not contain the SQLite database and the DRInventor Resources, but they are avaiable in:

	- SQLite database
		--> https://drive.google.com/file/d/1w_0IuFnvyDtbdsUCr0DzW7wF-nmUJhwL/view?usp=sharing

	- DRIresource-4.0 (it has to be extracted in the root of the project)
		--> https://drive.google.com/file/d/11-tN1s2gbU-nv8fFQQg3SB-sdf-gt4_0/view?usp=sharing

Contact:
	- Àlex Bravo: alex.bravo@upf.edu
	- Horacio Saggion: horacio.saggion@upf.edu

The code of this repository is distributed with a Creative Commons Attribution 4.0  license. More information in the LICENCE.TXT file.
