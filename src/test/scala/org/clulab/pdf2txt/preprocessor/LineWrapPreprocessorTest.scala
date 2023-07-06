package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test
import org.clulab.pdf2txt.languageModel.ProbabilisticLanguageModel

class LineWrapPreprocessorTest extends Test {

  class TestLanguageModel(vocab: Map[String, Float]) extends ProbabilisticLanguageModel {

    override def p(nextWord: String, prevWords: Seq[String]): Float  = {
      val context = prevWords.mkString(" ")
      val probability = vocab.getOrElse(nextWord, 0f)

      println(s"p($nextWord | $context) = $probability")
      probability
    }
  }

  val languageModel = {
    val words = Seq(
      "accuracy",
      "algorithms",
      "arrays",
      "assigned",
      "because",
      "Chapter",
      "components",
      "composed",
      "configuration",
      "consists",
      "correspond",
      "dictionary",
      "different",
      "dimension",
      "efficiently",
      "embeddings",
      "employed",
      "encouraging",
      "example",
      "format",
      "function",
      "However",
      "however",
      "Hugging",
      "implementation",
      "includes",
      "information",
      "instantiate",
      "label",
      "maximum",
      "networks",
      "number",
      "observations",
      "original",
      "outputs",
      "part-of-speech",
      "provides",
      "purpose",
      "representation",
      "situations",
      "specifically",
      "subsequent",
      "tensors",
      "tokenization",
      "tokenizer",
      "token",
      "tokens",
      "transformer",
      "Universidad",
      "unknown",
    )
    val map = words.map { word => word -> 1f }.toMap
    val languageModel = new TestLanguageModel(map)

    languageModel
  }
  val preprocessor = new LineWrapPreprocessor(languageModel)

  behavior of "LineWrapPreprocessor"

  def test(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  // chapter11_sentences.txt
  test(
  	"The previous chapter was our first exposure to recurrent neural net- works, which included intuitions for why they are useful for natural language processing, various architectures, and training algorithms.",
    "The previous chapter was our first exposure to recurrent neural networks, which included intuitions for why they are useful for natural language processing, various architectures, and training algorithms."
  )
  test(
  	"To take a break from NLP applications for English, in this chapter we use the AnCora corpus (Taul\u00E9 et al., 2008), which primarily con- sists of newspaper texts in Spanish and Catalan with different linguistic annotations.",
    "To take a break from NLP applications for English, in this chapter we use the AnCora corpus (Taul\u00E9 et al., 2008), which primarily consists of newspaper texts in Spanish and Catalan with different linguistic annotations."
  )
  test(
    "The data is distributed in the CoNLL-U for- mat.",
    "The data is distributed in the CoNLL-U format."
  )
  test(
    "meaning of the different fields is beyond the goal\",of this chapter; how- ever, the curious reader can find one at the CoNLL-U website.1",
    "meaning of the different fields is beyond the goal\",of this chapter; however, the curious reader can find one at the CoNLL-U website.1"
  )
  test(
    "Here we use the publicly-available GloVe embeddings trained on the Spanish Billion Word Corpus3 by the Departamento de Ciencias de la Computaci\u00F3n of Univer- sidad de Chile.4 In contrast to the GloVe embeddings used in Chapter 9, these do include a header that stores meta data about the embeddings (i.e., size of the vocabulary and the dimension of the embedding vectors), so in this case we do not use the no_header=True argument:",
    "Here we use the publicly-available GloVe embeddings trained on the Spanish Billion Word Corpus3 by the Departamento de Ciencias de la Computaci\u00F3n of Universidad de Chile.4 In contrast to the GloVe embeddings used in Chapter 9, these do include a header that stores meta data about the embeddings (i.e., size of the vocabulary and the dimension of the embedding vectors), so in this case we do not use the no_header=True argument:"
  )
  test(
    "Another difference between these GloVe embeddings and the ones we used in Chapter 9 is that these already include an embedding for un- known words.",
    "Another difference between these GloVe embeddings and the ones we used in Chapter 9 is that these already include an embedding for unknown words."
  )
  test(
    "  .	4 \u00A0https://github.com/dccuchile/spanish- word- embeddings# \u00A0\u2028glove- embeddings- from- sbwc \u00A0\u2028",
    "  .	4 \u00A0https://github.com/dccuchile/spanish- word- embeddings# \u00A0\u2028glove- embeddings- from- sbwc \u00A0\u2028"
  )
  test(
    "(From now on we will omit the pandas tables for readability, but, as usual, the corresponding Jupyter notebook contains all necessary infor- mation.)",
    "(From now on we will omit the pandas tables for readability, but, as usual, the corresponding Jupyter notebook contains all necessary information.)"
  )
  test(
    "We will implement this func- tion using PyTorch’s torch.nn.utils.rnn.pad_sequence() function, which, unsurprisingly, pads a group of tensors.",
    "We will implement this function using PyTorch’s torch.nn.utils.rnn.pad_sequence() function, which, unsurprisingly, pads a group of tensors."
  )
  test(
    "The model consists of: (a) an embedding layer for our Spanish pretrained embeddings; (b) an LSTM that can be set to be uni- or bi-directional (see Figure 10.3; the RNN is configured to be bidirectional by setting the bidirectional argument to True in the LSTM constructor), with a configurable num- ber of layers (see Figure 10.2; the number of layers is set through the num_layers argument of the constructor) and (c) a linear layer on top of each hidden state, which is used to predict the scores for each of the POS tags for the corresponding token.",
    "The model consists of: (a) an embedding layer for our Spanish pretrained embeddings; (b) an LSTM that can be set to be uni- or bi-directional (see Figure 10.3; the RNN is configured to be bidirectional by setting the bidirectional argument to True in the LSTM constructor), with a configurable number of layers (see Figure 10.2; the number of layers is set through the num_layers argument of the constructor) and (c) a linear layer on top of each hidden state, which is used to predict the scores for each of the POS tags for the corresponding token."
  )
  test(
    "Then we retrieve the em- beddings.",
    "Then we retrieve the embeddings."
  )
  test(
    "We next initialize all the hyper parameters and all the required com- ponents:",
    "We next initialize all the hyper parameters and all the required components:"
  )
  test(
    "One notable difference is that the output of this model has three dimensions instead of two: number of examples, number of to- kens, and number of POS tag scores.",
    "One notable difference is that the output of this model has three dimensions instead of two: number of examples, number of tokens, and number of POS tag scores."
  )
  test(
    "This is encour- aging considering that our approach does not include the CRF layer we discussed in Chapter 10.",
    "This is encouraging considering that our approach does not include the CRF layer we discussed in Chapter 10."
  )
  test(
    "In this chapter we have implemented a Spanish part-of-speech tagger using a bidirectional LSTM and a set of pretrained, static word em- beddings.",
    "In this chapter we have implemented a Spanish part-of-speech tagger using a bidirectional LSTM and a set of pretrained, static word embeddings."
  )
  test(
    "Through this process, we have also introduced several new PyTorch features such as the pad_sequence, pack_padded_sequence, and pad_packed_sequence functions, which allow us to work more e\uFB00iciently with variable length sequences for recurrent neural networks.",
    "Through this process, we have also introduced several new PyTorch features such as the pad_sequence, pack_padded_sequence, and pad_packed_sequence functions, which allow us to work more e\uFB00iciently with variable length sequences for recurrent neural networks."
  )

  // chapter13_sentences.txt
  test(
	"Intuitively, this strategy allows transformer net- works to achieve higher performance on smaller datasets by relying on statistics acquired at scale in an unsupervised way (e.g., through the masked language model training objective).",
    "Intuitively, this strategy allows transformer networks to achieve higher performance on smaller datasets by relying on statistics acquired at scale in an unsupervised way (e.g., through the masked language model training objective)."
  )
  test(
	"Using pre-trained trans- former encoders, we will implement the two tasks that served as use cases in the previous chapters: text classification and part-of-speech tagging.",
    "Using pre-trained transformer encoders, we will implement the two tasks that served as use cases in the previous chapters: text classification and part-of-speech tagging."
  )
  test(
    "To make this more concrete, we show below how tokenizers are em- ployed in the Hugging Face library.",
    "To make this more concrete, we show below how tokenizers are employed in the Hugging Face library."
  )
  test(
    "This is important for two reasons: (a) different transformers rely on different tokenization al- gorithms, and (b) even for the ones that use the same algorithm, their tokenizer vocabularies are likely to be different if they were pre-trained",
    "This is important for two reasons: (a) different transformers rely on different tokenization algorithms, and (b) even for the ones that use the same algorithm, their tokenizer vocabularies are likely to be different if they were pre-trained"
  )
  test(
    "For ex- ample, the output above shows that the word walrus was split into three sub-words.",
    "For example, the output above shows that the word walrus was split into three sub-words."
  )
  test(
    "Note, however, that this is specific to this particular tokeniza- tion algorithm, and other tokenizers may indicate word continuation in different ways.",
    "Note, however, that this is specific to this particular tokenization algorithm, and other tokenizers may indicate word continuation in different ways."
  )
  test(
    "Lastly, the input_ids attribute pro- vides the token ids used internally by the transformer to map tokens to embeddings.",
    "Lastly, the input_ids attribute provides the token ids used internally by the transformer to map tokens to embeddings."
  )
  test(
    "To briefly demonstrate how different tokenizers produce different out- puts, here is the same text tokenized with the tokenizer corresponding to xlm-roberta-base:",
    "To briefly demonstrate how different tokenizers produce different outputs, here is the same text tokenized with the tokenizer corresponding to xlm-roberta-base:"
  )
  test(
    "Now how- ever, rather than continuing with pandas, we will create a Hugging Face dataset from the dataframes.",
    "Now however, rather than continuing with pandas, we will create a Hugging Face dataset from the dataframes."
  )
  test(
    "Hugging Face datasets are convenient be- cause of their built-in support of batching, e\uFB00icient data transformations, and caching.",
    "Hugging Face datasets are convenient because of their built-in support of batching, e\uFB00icient data transformations, and caching."
  )
  test(
    "Note that this is the same data structure seen when downloading a Hug- ging Face dataset from their hub.2",
    "Note that this is the same data structure seen when downloading a Hugging Face dataset from their hub.2"
  )
  test(
    "Different pre-trained models are tokenized differently, and it is important to select the to- kenizer that corresponds to the model we will use so that the inputs are consistent with model expectations.",
    "Different pre-trained models are tokenized differently, and it is important to select the tokenizer that corresponds to the model we will use so that the inputs are consistent with model expectations."
  )
  test(
    "How- ever",
    "However"
  )
  test(
    ", since this is a simple scenario, all we need to do is provide the text to tokenize and specify how to handle texts that exceed the max- imum number of tokens permitted by the pre-trained model.",
    ", since this is a simple scenario, all we need to do is provide the text to tokenize and specify how to handle texts that exceed the maximum number of tokens permitted by the pre-trained model."
  )
  test(
    "Further, we also want to remove some of the columns that are no longer needed, simplifying sub- sequent steps.",
    "Further, we also want to remove some of the columns that are no longer needed, simplifying subsequent steps."
  )
  test(
    "Models that implement specific downstream tasks are usually com- posed of a pre-trained model (sometimes referred as the body), and one or more task-specific layers (usually referred as the head).",
    "Models that implement specific downstream tasks are usually composed of a pre-trained model (sometimes referred as the body), and one or more task-specific layers (usually referred as the head)."
  )
  test(
    "Our implemen- tation of the forward pass sends the input tokens to the Bert model to produce the contextualized representations for all tokens.",
    "Our implementation of the forward pass sends the input tokens to the Bert model to produce the contextualized representations for all tokens."
  )
  test(
    "As in the previous chapters, we apply dropout to our sequence rep- resentation, and then pass it through our linear classification layer.",
    "As in the previous chapters, we apply dropout to our sequence representation, and then pass it through our linear classification layer."
  )
  test(
    "Next we load the configuration of the pre-trained model and instanti- ate our model.",
    "Next we load the configuration of the pre-trained model and instantiate our model."
  )
  test(
    "With this call, the pre-trained model will be loaded, which in- cludes downloading if necessary:",
    "With this call, the pre-trained model will be loaded, which includes downloading if necessary:"
  )
  test(
    "This class not only implements the training loop we have been using in the previous chapters, but also handles other useful steps such as saving checkpoints (i.e., intermediate models after a num- ber of mini-batches have been processed during training), and track-",
    "This class not only implements the training loop we have been using in the previous chapters, but also handles other useful steps such as saving checkpoints (i.e., intermediate models after a number of mini-batches have been processed during training), and track-"
  )
  test(
    "The expected return type is a dictio- nary whose keys correspond to different metrics, each of which will be displayed as a separate result column.",
    "The expected return type is a dictionary whose keys correspond to different metrics, each of which will be displayed as a separate result column."
  )
  test(
    "Also, the trainer will automatically use any GPU that is available, unless specifi- cally disabled in the TrainingArguments.",
    "Also, the trainer will automatically use any GPU that is available, unless specifically disabled in the TrainingArguments."
  )
  test(
    "However, the Trainer class pro- vides a predict() method that drastically simplifies this:",
    "However, the Trainer class provides a predict() method that drastically simplifies this:"
  )
  test(
    "The orig- inal BERT paper (Devlin et al., 2018) addresses this by only using the embedding corresponding to the first sub-token for each word.",
    "The original BERT paper (Devlin et al., 2018) addresses this by only using the embedding corresponding to the first sub-token for each word."
  )
  test(
    "For the sub-words that do not corre- spond to the beginning of a word, we use a special value that indicates that we are not interested in their predictions.",
    "For the sub-words that do not correspond to the beginning of a word, we use a special value that indicates that we are not interested in their predictions."
  )
  test(
    "We discussed in the text classification section that Hugging Face pro- vides implementations for text classification models.",
    "We discussed in the text classification section that Hugging Face provides implementations for text classification models."
  )
  test(
    "The number of labels which determines the output di- mension of the linear layer is equal to the number of POS tags.",
    "The number of labels which determines the output dimension of the linear layer is equal to the number of POS tags."
  )
  test(
    "The primary difference between the text classification example and this to- ken classification model is that with the former we produced one la- bel for each text document, while here we produce one label for each token in the input text.",
    "The primary difference between the text classification example and this token classification model is that with the former we produced one label for each text document, while here we produce one label for each token in the input text."
  )
  test(
    "For this pur- pose, we use the view() method to reshape the tensors.",
    "For this purpose, we use the view() method to reshape the tensors."
  )
  test(
    "Instead it provides a new view of the same data that behaves like a tensor with a differ- ent shape.7 As mentioned before, the number of arguments passed to this method determines the number of dimensions in the output tensor.",
    "Instead it provides a new view of the same data that behaves like a tensor with a different shape.7 As mentioned before, the number of arguments passed to this method determines the number of dimensions in the output tensor."
  )
  test(
    "Next, we instantiate our model using the XLM-RoBERTa configura- tion:",
    "Next, we instantiate our model using the XLM-RoBERTa configuration:"
  )
  test(
    "This function expects two one- dimensional lists of labels, so we need to follow a similar approach to the one we employed for text classification.",
    "This function expects two one- dimensional lists of labels, so we need to follow a similar approach to the one we employed for text classification."
  )
  test(
    "Note that output.label_ids and output.predictions are NumPy arrays rather than PyTorch ten- sors.",
    "Note that output.label_ids and output.predictions are NumPy arrays rather than PyTorch tensors."
  )
  test(
    "This time we use NumPy\u2019s reshape() method to reshape the ar- rays.",
    "This time we use NumPy\u2019s reshape() method to reshape the arrays."
  )
  test(
    "This method is similar to PyTorch\u2019s view() method that we used previously, except that view() may copy the array’s data in some situ- ations.",
    "This method is similar to PyTorch\u2019s view() method that we used previously, except that view() may copy the array’s data in some situations."
  )
  test(
    "This is considerably better than the LSTM-based model developed in Chap- ter 11.",
    "This is considerably better than the LSTM-based model developed in Chapter 11."
  )
  test(
    "In the confusion matrices shown below, each cell xij corresponds to the proportion of values with label i that were as- signed the label j.8",
    "In the confusion matrices shown below, each cell xij corresponds to the proportion of values with label i that were assigned the label j.8"
  )
  test(
    "Figure 13.1 Confusion matrix corresponding to the LSTM-based part-of- speech tagger developed in Chapter 11.",
    "Figure 13.1 Confusion matrix corresponding to the LSTM-based part-of- speech tagger developed in Chapter 11."
  )
  test(
    "The two confusion matrices highlight a couple of important observa- tions.",
    "The two confusion matrices highlight a couple of important observations."
  )
  test(
    "For example, the ac- curacy for predicting the SYM POS tag increased from 38% in the LSTM model to 95% in the transformer model!",
    "For example, the accuracy for predicting the SYM POS tag increased from 38% in the LSTM model to 95% in the transformer model!"
  )
  test(
    "Equally as impressive, the trans- former improved the performance of tags that are extremely common, and, thus, provide plenty of opportunity to both approaches to learn a good model.",
    "Equally as impressive, the transformer improved the performance of tags that are extremely common, and, thus, provide plenty of opportunity to both approaches to learn a good model."
  )
}
