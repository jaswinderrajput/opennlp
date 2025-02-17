/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.formats.masc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenizerEvaluator;
import opennlp.tools.tokenize.TokenizerFactory;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class MascTokenSampleStreamTest {

  @Test
  public void read() {
    try {
      FileFilter fileFilter = pathname -> pathname.getName().contains("MASC");
      File directory = new File(this.getClass().getResource(
          "/opennlp/tools/formats/masc/").getFile());
      MascTokenSampleStream stream;
      stream = new MascTokenSampleStream(
          new MascDocumentStream(directory, true, fileFilter));

      TokenSample s = stream.read();

      String expectedString = "This is a test Sentence.";
      assertEquals(expectedString, s.getText());

      Span[] expectedTags = {
          new Span(0, 4),
          new Span(5, 7),
          new Span(8, 9),
          new Span(10, 14),
          new Span(15, 23),
          new Span(23, 24)};
      assertArrayEquals(expectedTags, s.getTokenSpans());

      s = stream.read();
      String expectedTokens = "This is 'nother test sentence.";
      assertEquals(expectedTokens, s.getText());

      expectedTags = new Span[] {
          new Span(0, 4),
          new Span(5, 7),
          new Span(8, 15),
          new Span(16, 20),
          new Span(21, 29),
          new Span(29, 30)};
      assertArrayEquals(expectedTags, s.getTokenSpans());
    } catch (IOException e) {
      fail("IO Exception: " + e.getMessage());
    }
  }

  @Test
  public void close() {
    try {
      FileFilter fileFilter = pathname -> pathname.getName().contains("MASC");
      File directory = new File(this.getClass().getResource(
          "/opennlp/tools/formats/masc/").getFile());
      MascTokenSampleStream stream;
      stream = new MascTokenSampleStream(
          new MascDocumentStream(directory, true, fileFilter));

      stream.close();
      TokenSample s = stream.read();
    } catch (IOException e) {
      assertEquals(e.getMessage(),
          "You are reading an empty document stream. " +
              "Did you close it?");
    }
  }

  @Test
  public void reset() {
    try {
      FileFilter fileFilter = pathname -> pathname.getName().contains("MASC");
      File directory = new File(this.getClass().getResource(
          "/opennlp/tools/formats/masc/").getFile());
      MascTokenSampleStream stream;
      stream = new MascTokenSampleStream(
          new MascDocumentStream(directory, true, fileFilter));

      TokenSample s = stream.read();
      s = stream.read();
      s = stream.read();
      assertNull(s);  //The stream should be exhausted by now

      stream.reset();

      s = stream.read();

      String expectedString = "This is a test Sentence.";
      assertEquals(expectedString, s.getText());

      Span[] expectedTags = {
          new Span(0, 4),
          new Span(5, 7),
          new Span(8, 9),
          new Span(10, 14),
          new Span(15, 23),
          new Span(23, 24)};
      assertArrayEquals(expectedTags, s.getTokenSpans());

    } catch (IOException e) {
      fail("IO Exception: " + e.getMessage());
    }
  }


  @Test
  public void train() {
    try {
      File directory = new File(this.getClass().getResource(
          "/opennlp/tools/formats/masc/").getFile());
      FileFilter fileFilter = pathname -> pathname.getName().contains("");
      ObjectStream<TokenSample> trainTokens = new MascTokenSampleStream(
          new MascDocumentStream(directory,
              true, fileFilter));

      System.out.println("Training");
      TokenizerModel model = null;
      TrainingParameters trainingParameters = new TrainingParameters();
      trainingParameters.put(TrainingParameters.ITERATIONS_PARAM, 20);

      model = TokenizerME.train(trainTokens, new TokenizerFactory("en", null, false, null),
          trainingParameters);

      ObjectStream<TokenSample> testTokens = new MascTokenSampleStream(
          new MascDocumentStream(directory,
              true, fileFilter));
      TokenizerEvaluator evaluator = new TokenizerEvaluator(new TokenizerME(model));
      evaluator.evaluate(testTokens);
      System.out.println(evaluator.getFMeasure());

    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println(Arrays.toString(e.getStackTrace()));
      fail("Exception raised");
    }


  }


}
