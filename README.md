# Thesis: Wikipedia Matching

**University of Trento**

**Name:** HOANG DUC MANH

**ID No.:** MAT.180387

## 1. Introduction

Wikipedia matching is a Java program which is responsible for detecting the translating contribution on Wikipedia articles. Wikipedia is a huge encyclopedia which allows authors (or its users) contributing their work at any article, at anytime. On Wikipedia, there are many possible language editions, which are representing a topic on may possible language edition articles. That is the reason why, some of the language edition articles are interpreted from other language edition articles. In order to detect this issue, Wikipedia matching is demanded  to be defined and built.

## 2. Architecture overview

Wikipedia matching system is a composite software consists of 3 main component which are (1) Wikiepdia crawling and translation module, (2) text similarity module and (3) post-processing and evaluation module. Here is an architecture overview of Wikipedia matching system.

![](pictures/1.png?raw=true)

Figure 1.  an architecture overview of Wikipedia matching system.

**Wikiepdia crawling and translation module** is a web service API component which undertakes getting the dataset from MeidaWiki API which is a web service API of Wikipedia resources and translating the non-English articles into English. Actually,  Wikiepdia crawling and translation module is composed by two sup-components which are (1) Wikipedia crawling and (2) Yandex translation.

* Wikipedia crawling is a client of MediaWiki API allowing its client to access the Wikipedia resources by using a HTTP request. The response of a request is a data depending on the parameters of the request. MediaWiki API allows us to query the data related to article revisions, article's information, author's contribution, article's language editions, etc.
* Yandex translation is a client of Yandex.Translate API permitting its client to access the Yandex translation machine and resources by using HTTP request. The HTTP request is built by using a original text and a direction of the translation. The data of a response is a text in destination language.

**Text similarity module** is a text similarity computation component which mainly computes the text similarity between each pair of documents which is associated from a main (or suspicious) document and the reference documents. The text similarity computing is performed on sentence level meaning that, each document in a pair should be segmented into sentences. Then, the computing is going to create a scores matrix. As we know, every text mining application have to do text preprocessing firstly to eliminate all the redundant and unimportant words, characters, etc to enhance the accuracy of the text similarity scores. Therefore, this component is also included a number of text preprocessing task such as text segmentation, part-of-speech tagging, lemmatization and stop words removing. There are two text similarity measure to be used such as (1) Cosine similarity and (2) Word N-Gram Jaccard measure. They are just to help us to have a comparison to select the best approach. It turns out that, Word N-Gram Jaccard measure is the best choice.

**Post-processing and evaluation module** is mainly responsible for looking at the scores matrices and looking for the important information called diagonals that can conclude on the translation plagiarism. There are two algorithm is devised such as “looking for diagonals using standard deviation” and “looking for diagonals using highest value”. It is having the same idea to be the comparison to select the best thing. Finally, we see that using “looking for diagonals using highest value” algorithm is the best choice. The evaluation part is going to print out all the important extracted information such as the highest value of the diagonals in a scores matrix, percentage of the translation plagiarism and the prediction which are going to see after.

## 3. Implementation

The implementation of the Wikipedia matching system mainly bases on number of library APIs and  the Apache UIMA framework (UIMA).
The library APIs are using for building the Wikipedia crawler and Yandex translator such as Jersey client APIs and Yandex translator java API. They are supporting to implement the clients of MediaWiki API and Yandex.Translate API to to create a RESTful Java client to perform “GET” and “POST” requests to REST service.

The UIMA is a Java framework provides the tool to work on natural language processing (NLP). They are supporting to build a pipeline to process data. We called it pipeline because it containing number of text preprocessing tasks, the result of a task is a input of next task. Basically, the input of the pipeline is a raw text and we by some how extract the important information in the raw text.

In order to develop the Wikipedia matching system, we use the Eclipse IDE which is a most popular Java interface development editor. We also add the Jersey client APIs, Yandex translator java API and UIMA framework as the external library APIs.

The following sub-section we are going to go through in details of the all components in the Wikipedia matching system.

**Wikipedia crawler and Yandex translator**

Here is the design of this component:

![](pictures/2.png?raw=true)

Figure 2. The architecture of Wikipedia crawler and Yandex translator.

This component basically get the Wikipedia articles based on an user name of an author engaged in Wikipedia contribution and translates all the non-English documents into English documents. The Wikipedia crawler gets a main article of an author and also finds the other language edition articles of the main article such that, finally we have a topic with 4 language editions in English, French, Italian, German. The Yandex translator is going to translate non-English documents into English documents. This component results the list of topics that an author who contributed on Wikipedia. Each topic contains 4 language edition articles and all of them are translated into English.

**Text similarity computation and post-processing and evaluation**

Here is the design of the component:

![](pictures/3.png?raw=true)

Figure 3. The architecture of text similarity computation and post-processing and evaluation.

Those documents are in the result of the Wikipedia crawler and Yandex translator component to be the input of text similarity computation and post-processing and evaluation. As we see on Figure 3, this component is going to do preprocessing task firstly to extract the important words, characters from the raw text of those input documents. Also, in preprocessing step, we are going to represent a document as a list of sentences and each sentence as a list of words. This is aiming to compute text similarity at the sentence level. Then, we put the preprocessing task's output into the text similarity computation sup-component. The text similarity computation results a matrix for each pair of documents. The matrix is basically called scores matrix which is the input of the post-processing task. The post-processing task is going to use “looking for diagonals using highest value” to eliminate the non-important scores in the matrix and use other devising algorithm to find the diagonals inside the matrix. Depending on the diagonals, we are going to extract the evaluation information and print them into the table called evaluation table.

## 4. Result

There are there important result such as:

**Diagonal matrix:**

![](pictures/4.png?raw=true)

**Evaluation table:**

![](pictures/5.png?raw=true)

**Document in markup view:**

![](pictures/6.png?raw=true)

![](pictures/7.png?raw=true)

## 5. How to run it

* All the external libraries are in the pom.xml file.

* There is the main class placed in “application” package.

* Run by executing the main class.

## 6. Acknowledgements

I am grateful to professor Marco Ronchetti who is my supervisor and co-instructor at University of Trento. He taught me during the time I studied Web architechture course. He has been supervising my study progress in all the time of my studying in University of Trento.  I thank to him for introducing me to this research and supervising the thesis progess. He gave me his suggestion, advice, encouragement and direction for doing this thesis. He also understood my studying situation to give me the appropriate studying direction.
