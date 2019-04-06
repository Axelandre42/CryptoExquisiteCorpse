/* 
 * Copyright 2019 Alexandre Waeles
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ovh.axelandre42.exquisitecorpse;

import java.io.File;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import ovh.axelandre42.exquisitecorpse.util.BiKeyMap;

/**
 * @author Alexandre Waeles <www.axelandre42.ovh>
 *
 */
public class ExquisiteCorpse {
    public static class SentenceAnalyzer {
        private String[] tokens;
        private Dictionary dict;

        public SentenceAnalyzer(Dictionary dict, String[] tokens) {
            this.dict = dict;
            this.tokens = tokens;
        }

        public long findLong() {
            long value = 0;

            for (int i = tokens.length - 1; i >= 0; i--) {
                String[] elements = tokens[i].split(":");
                int v, mod;
                if (elements[0].equals("Adj")) {
                    v = dict.getIndexForAdjective(elements[1]);
                    mod = dict.getAdjectiveAmount();
                } else if (elements[0].equals("Nom")) {
                    v = dict.getIndexForNoun(elements[1]);
                    mod = dict.getNounAmount();
                } else if (elements[0].equals("Ver")) {
                    v = dict.getIndexForVerb(elements[1]);
                    mod = dict.getVerbAmount();
                } else if (elements[0].equals("Adv")) {
                    v = dict.getIndexForAdverb(elements[1]);
                    mod = dict.getAdverbAmount();
                } else {
                    continue;
                }
                value *= mod;
                value += v;
            }

            return value;
        }
    }

    public static class SentenceBuilder {
        private List<Word> sequence = new ArrayList<>();
        private long value;
        private Dictionary dict;

        public SentenceBuilder(Dictionary dict, long value) {
            this.dict = dict;
            this.value = value;
        }

        public SentenceBuilder addNoun() {
            int index = (int) (value % (dict.getNounAmount()));
            sequence.add(dict.selectNoun(index));
            value /= (dict.getNounAmount());

            return this;
        }

        public SentenceBuilder addAdjective() {
            int index = (int) (value % (dict.getAdjectiveAmount()));
            Noun lastNoun = getLastNoun();
            sequence.add(dict.selectAdjective(lastNoun.amount, lastNoun.gender, index));
            value /= (dict.getAdjectiveAmount());

            return this;
        }

        public SentenceBuilder addVerb() {
            int index = (int) (value % (dict.getVerbAmount()));
            Noun lastNoun = getLastNoun();
            sequence.add(dict.selectVerb(lastNoun.amount, index));
            value /= (dict.getVerbAmount());

            return this;
        }

        public SentenceBuilder addAdverb() {
            int index = (int) (value % (dict.getAdverbAmount()));
            sequence.add(dict.selectAdverb(index));
            value /= (dict.getAdverbAmount());

            return this;
        }

        private Noun getLastNoun() {
            int i = 1;
            Word w;
            do {
                w = sequence.get(sequence.size() - i);
                i++;
            } while (!(w instanceof Noun));
            return (Noun) w;
        }

        public long getRemaining() {
            return this.value;
        }

        public int getMinimalForm() {
            if (value < computePossibilities(2, 1, 1, 0))
                return 1;
            if (value < computePossibilities(2, 2, 1, 0))
                return 2;
            if (value < computePossibilities(2, 2, 1, 1))
                ;
            return 3;
        }

        private long computePossibilities(int nounAmount, int adjectiveAmount, int verbAmount, int adverbAmount) {
            return (long) (Math.pow(dict.getNounAmount(), nounAmount)
                    * Math.pow(dict.getAdjectiveAmount(), adjectiveAmount) * Math.pow(dict.getVerbAmount(), verbAmount)
                    * Math.pow(dict.getAdverbAmount(), adverbAmount));
        }

        public String build() {
            String output = "";
            int i = 0;
            for (Word word : sequence) {
                if ((i != 0) && (i < (sequence.size()))) {
                    output += " ";
                }
                if (word instanceof Noun) {
                    output += dict.selectDetForNoun((Noun) word);
                }
                output += word.word;
                i++;
            }

            String upper = new StringBuffer().insert(0, output.charAt(0)).toString().toUpperCase();

            return upper + output.substring(1) + ".";
        }
        
        public String buildAnnotated() {
            String output = "";
            int i = 0;
            for (Word word : sequence) {
                if ((i != 0) && (i < (sequence.size()))) {
                    output += " ";
                }

                if (word instanceof Noun) {
                    output += "Nom:";
                }
                if (word instanceof Adjective) {
                    output += "Adj:";
                }
                if (word instanceof Verb) {
                    output += "Ver:";
                }
                if (word instanceof Adverb) {
                    output += "Adv:";
                }
                
                output += word.word;
                i++;
            }

            return output;
        }
    }

    public static class WordIndex implements Comparable<WordIndex> {
        public String name;
        public boolean shouldHashBasedOnIndex = false;
        public int index;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof WordIndex) {
                WordIndex objWI = (WordIndex) obj;
                if (objWI.index == this.index || (objWI.name != null && objWI.name.equals(this.name))) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return shouldHashBasedOnIndex ? index : name.hashCode();
        }

        @Override
        public int compareTo(WordIndex o) {
            return (int) Math.signum(this.index - o.index);
        }

    }

    public static abstract class Word {
        public String word;
    }

    public static class Noun extends Word {
        public String gender;
        public String amount;

        @Override
        public String toString() {
            return String.format("(%s) Type=Nom G: %s A: %s", word, gender, amount);
        }
    }

    public static class Adjective extends Word {

        public String gender;
        public String amount;

        public String type;

        @Override
        public String toString() {
            return String.format("(%s) Type=Adj G: %s A: %s T: %s", word, gender, amount, type);
        }
    }

    public static class Verb extends Word {
        public String tense;

        public String amount;
        public String person;

        // PPas only.
        public String gender;

        @Override
        public String toString() {
            return String.format("(%s) Type=Ver T: %s A: %s P: %s G: %s", word, tense, amount, person, gender);
        }
    }

    public static class Adverb extends Word {

        @Override
        public String toString() {
            return "(" + word + ") Type=Adv";
        }

    }

    public static class Dictionary {

        private BiKeyMap<Integer, String, List<Noun>> nouns = new BiKeyMap<>();
        private BiKeyMap<Integer, String, List<Adjective>> adjectives = new BiKeyMap<>();
        private BiKeyMap<Integer, String, List<Verb>> verbs = new BiKeyMap<>();
        private BiKeyMap<Integer, String, List<Adverb>> adverbs = new BiKeyMap<>();

        private int a = 0, b = 0, c = 0, d = 0;

        public void load(File dictionary) {
            try {
                Scanner sc = new Scanner(dictionary);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String[] elements = line.split("[\t ]");
                    String[] meta = elements[2].split(":");

                    if (meta[0].equals("Adv")) {
                        Adverb adverb = new Adverb();
                        adverb.word = elements[0];
                        if (adverbs.containsKeyB(elements[1])) {
                            adverbs.getFromB(elements[1]).add(adverb);
                        } else {
                            List<Adverb> newList = new ArrayList<>();
                            newList.add(adverb);
                            adverbs.put(a, elements[1], newList);
                            a++;
                        }
                    }

                    if (meta[0].equals("Nom")) {
                        String[] data = meta[1].split("\\+");
                        Noun noun = new Noun();
                        noun.word = elements[0];
                        if (data.length == 2) {
                            noun.gender = data[0];
                            noun.amount = data[1];
                        } else {
                            noun.amount = data[0];
                        }
                        if (nouns.containsKeyB(elements[1])) {
                            nouns.getFromB(elements[1]).add(noun);
                        } else {
                            List<Noun> newList = new ArrayList<>();
                            newList.add(noun);
                            nouns.put(b, elements[1], newList);
                            b++;
                        }
                    }

                    if (meta[0].equals("Adj")) {
                        String[] data = meta[1].split("\\+");
                        Adjective adjective = new Adjective();
                        adjective.word = elements[0];
                        if (data.length == 2) {
                            adjective.gender = data[0];
                            adjective.amount = data[1];
                        } else {
                            adjective.type = data[0];
                        }
                        if (adjectives.containsKeyB(elements[1])) {
                            adjectives.getFromB(elements[1]).add(adjective);
                        } else {
                            List<Adjective> newList = new ArrayList<>();
                            newList.add(adjective);
                            adjectives.put(c, elements[1], newList);
                            c++;
                        }
                    }

                    if (meta[0].equals("Ver")) {
                        for (int i = 1; i < meta.length; i++) {
                            String[] data = meta[i].split("\\+");
                            Verb verb = new Verb();
                            verb.word = elements[0];
                            verb.tense = data[0];
                            if (!(verb.tense.equals("PPas") || verb.tense.equals("PPre") || verb.tense.equals("Inf"))) {
                                verb.amount = data[1];
                                verb.person = data[2];
                            } else if (verb.tense.equals("PPas") && data.length == 3) {
                                verb.gender = data[1];
                                verb.amount = data[2];
                            }
                            if (verbs.containsKeyB(elements[1])) {
                                verbs.getFromB(elements[1]).add(verb);
                            } else {
                                List<Verb> newList = new ArrayList<>();
                                newList.add(verb);
                                verbs.put(d, elements[1], newList);
                                d++;
                            }
                        }
                    }
                }
                sc.close();
            } catch (Exception e) {
                throw new RuntimeException("Exception during dictionary load", e);
            }

        }

        public int getNounAmount() {
            return nouns.size() / 4;
        }

        public int getAdjectiveAmount() {
            return adjectives.size() / 4;
        }

        public int getVerbAmount() {
            return verbs.size() / 8;
        }

        public int getAdverbAmount() {
            return adverbs.size();
        }

        public int getIndexForNoun(String noun) {
            List<List<Noun>> values = new ArrayList<>(nouns.values());
            List<Noun> matching = values.stream().filter(v -> v.stream().anyMatch(v1 -> v1.word.equals(noun))).findAny()
                    .orElse(null);
            if (matching == null)
                return -1;
            List<Integer> indexes = new ArrayList<>(nouns.keysA());
            int index = indexes.stream().filter(i -> nouns.getFromA(i).equals(matching)).findAny().get();
            return index / 4;
        }

        public int getIndexForAdjective(String adjective) {
            List<List<Adjective>> values = new ArrayList<>(adjectives.values());
            List<Adjective> matching = values.stream().filter(v -> v.stream().anyMatch(v1 -> v1.word.equals(adjective)))
                    .findAny().orElse(null);
            if (matching == null)
                return -1;
            List<Integer> indexes = new ArrayList<>(adjectives.keysA());
            int index = indexes.stream().filter(i -> adjectives.getFromA(i).equals(matching)).findAny().get();
            return index / 4;
        }

        public int getIndexForVerb(String verb) {
            List<List<Verb>> values = new ArrayList<>(verbs.values());
            List<Verb> matching = values.stream().filter(v -> v.stream().anyMatch(v1 -> v1.word.equals(verb))).findAny()
                    .orElse(null);
            if (matching == null)
                return -1;
            List<Integer> indexes = new ArrayList<>(verbs.keysA());
            int index = indexes.stream().filter(i -> verbs.getFromA(i).equals(matching)).findAny().get();
            return index / 8;
        }

        public int getIndexForAdverb(String adverb) {
            List<List<Adverb>> values = new ArrayList<>(adverbs.values());
            List<Adverb> matching = values.stream().filter(v -> v.stream().anyMatch(v1 -> v1.word.equals(adverb)))
                    .findAny().orElse(null);
            if (matching == null)
                return -1;
            List<Integer> indexes = new ArrayList<>(adverbs.keysA());
            int index = indexes.stream().filter(i -> adverbs.getFromA(i).equals(matching)).findAny().get();
            return index;
        }

        public String selectDetForNoun(Noun noun) {
            Random rand = new Random();
            String subjectDet = "";
            boolean random = rand.nextBoolean();
            boolean startsWithVowel = startsWithVowel(noun);
            if (noun.amount.equals("PL"))
                subjectDet = random ? "les " : "des ";
            else if (noun.gender.equals("Fem"))
                subjectDet = startsWithVowel ? (random ? "l'" : "une ") : (random ? "la " : "une ");
            else
                subjectDet = startsWithVowel ? (random ? "l'" : "un ") : (random ? "le " : "un ");
            return subjectDet;
        }

        public boolean startsWithVowel(Noun noun) {
            String[] vowels = { "a", "e", "i", "o", "u", "y", "h" };

            for (String vowel : vowels) {
                if (noun.word.startsWith(vowel))
                    return true;
            }
            return false;
        }

        public Noun selectNoun(int in) {
            Random rand = new Random();
            List<Noun> selected;
            int index = 4 * in;
            do {
                List<Noun> found = nouns.getFromA(index);
                selected = found.stream().filter(n -> (n.gender.equals("Mas") || n.gender.equals("Fem"))
                        && (n.amount.equals("SG") || n.amount.equals("PL"))).collect(Collectors.toList());
                index++;
                if (index - in > 3) {
                    return found.get(0);
                }
            } while (selected.size() == 0);
            int index2 = (int) (rand.nextDouble() * selected.size());
            return selected.get(index2);
        }

        public Verb selectVerb(String amount, int in) {
            Random rand = new Random();
            List<Verb> selected;
            int index = 8 * in;
            do {
                List<Verb> found = verbs.getFromA(index);
                selected = found.stream()
                        .filter(v -> !(v.tense.equals("PPre") || v.tense.equals("PPas") || v.tense.equals("Inf"))
                                && v.amount.equals(amount) && v.person.equals("P3"))
                        .collect(Collectors.toList());
                index++;
                if (index - 8 * in > 7) {
                    return found.get(0);
                }
            } while (selected.size() == 0);
            int index2 = (int) (rand.nextDouble() * selected.size());
            return selected.get(index2);
        }

        public Adjective selectAdjective(String amount, String gender, int in) {
            Random rand = new Random();
            List<Adjective> selected;
            int index = 4 * in;
            do {
                List<Adjective> found = adjectives.getFromA(index);
                index++;
                selected = found.stream().filter(a -> (a.type == null) && a.amount.equals(amount)
                        && (a.gender == null ? false : a.gender.equals(gender))).collect(Collectors.toList());
                if (index - in > 3) {
                    return found.get(0);
                }
            } while (selected.size() == 0);

            int index2 = (int) (rand.nextDouble() * selected.size());
            return selected.get(index2);
        }

        public Adverb selectAdverb(int index) {
            Random rand = new Random();
            List<Adverb> selected = adverbs.getFromA(index);

            int index2 = (int) (rand.nextDouble() * selected.size());
            return selected.get(index2);
        }
    }

    public static void main(String[] args) {
        Dictionary dict = new Dictionary();
        try {
            File dir = new File("dict");
            for (String filename : dir.list()) {
                dict.load(new File(dir, filename));
                System.out.printf("Loading %s...\n", filename);
            }

            DH dh = new DH();

            byte[] enc = dh.generatePublicKey().getEncoded();
            
            System.out.println("Say this to your friend:");
            
            Longifier longifier = new Longifier(enc);
            int i = 1;
            while (longifier.hasNext()) {
                long randLong = longifier.nextLong();
                SentenceBuilder builder = new SentenceBuilder(dict, randLong);
                processsSenctence(builder);
                //System.out.printf("Sentence %d: %s\n", i, builder.build());
                System.out.printf("%s\n", builder.buildAnnotated());
                i++;
            }

            System.out.println("Write what your friend said (ends with an empty line):");
            System.out.println("You must write in the form [<type>:<word> <type>:<word> <type>:<word> ...].");
            System.out.println("Valid types are \"Nom\", \"Adj\", \"Ver\" and \"Adv\".");
            Scanner sc = new Scanner(System.in);
            Delongifier delongifier = new Delongifier();
            
            boolean shouldContinue = true;
            do {
                System.out.print("> ");
                String input = sc.nextLine();
                if (input.isEmpty()) {
                    shouldContinue = false;
                } else {
                    SentenceAnalyzer anal = new SentenceAnalyzer(dict, input.split(" "));
                    long found = anal.findLong();
                    delongifier.pushLong(found);
                }
            } while (shouldContinue);
            byte[] bytes = delongifier.toByteArray();
            KeyFactory fact = KeyFactory.getInstance("EC");
            PublicKey prekey = fact.generatePublic(new X509EncodedKeySpec(bytes));
            SecretKey key = dh.computeSharedKey(prekey);
            shouldContinue = true;
            AES aes = new AES(key);
            do {
                System.out.println("Do you want to encode (1) or decode (2)?");
                System.out.print("> ");
                String line = sc.nextLine();
                int choice = Integer.decode(line);
                switch (choice) {
                case 1:
                    boolean shouldContinue2 = true;
                    String text = "";
                    do {
                        System.out.println("Write whatever you want to encode (ends with an empty line):");
                        System.out.print("> ");
                        
                        String line2 = sc.nextLine();
                        if (line2.isEmpty()) {
                            shouldContinue2 = false;
                        } else {
                            text += line2 + "\n";
                        }
                    } while (shouldContinue2);
                    byte[] newEncoded = aes.encode(text.getBytes());
                    System.out.println("Say this to your friend:");
                    longifier = new Longifier(newEncoded);
                    int j = 1;
                    while (longifier.hasNext()) {
                        long randLong = longifier.nextLong();
                        SentenceBuilder builder = new SentenceBuilder(dict, randLong);
                        processsSenctence(builder);
                        System.out.printf("Sentence %d: %s\n", j, builder.build());
                        j++;
                    }
                    break;
                case 2:
                    System.out.println("Write what your friend said (ends with an empty line):");
                    System.out.println("You must write in the form [<type>:<word> <type>:<word> <type>:<word> ...].");
                    System.out.println("Valid types are \"Nom\", \"Adj\", \"Ver\" and \"Adv\".");
                    
                    Delongifier delongifier2 = new Delongifier();
                    shouldContinue2 = true;
                    do {
                        System.out.print("> ");
                        String input = sc.nextLine();
                        if (input.isEmpty()) {
                            shouldContinue2 = false;
                        } else {
                            SentenceAnalyzer anal = new SentenceAnalyzer(dict, input.split(" "));
                            long found = anal.findLong();
                            delongifier2.pushLong(found);
                        }
                    } while (shouldContinue2);
                    byte[] bytes2 = delongifier2.toByteArray();
                    String result = new String(aes.decode(bytes2));

                    System.out.printf("Result: %s\n", result);

                default:
                    break;
                }
            } while (shouldContinue);
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    private static void processsSenctence(SentenceBuilder builder) {
        int formQualifier = builder.getMinimalForm();

        switch (formQualifier) {
        case 1:
            builder.addNoun().addVerb().addNoun().addAdjective();
            break;
        case 2:
            builder.addNoun().addAdjective().addVerb().addNoun().addAdjective();
            break;
        case 3:
            builder.addNoun().addAdjective().addVerb().addAdverb().addNoun().addAdjective();
            break;
        }
    }
}
