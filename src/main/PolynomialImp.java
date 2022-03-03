package main;

import java.util.Iterator;

public class PolynomialImp implements Polynomial {

    private ArrayList<Term> termsList;

    public PolynomialImp(String polynomial){
        this.termsList = new ArrayList<>();
        String[] splitPoly = polynomial.split("\\+");

        for(String term: splitPoly) {
            this.termsList.add(this.stringToTerm(term));
        }
    }
    //Converts a string into terms
    //Find the coefficient then the exponent, if not "x" is found returns the term with exponent 0
    private Term stringToTerm(String term){
        double temp;

        if(term.contains("x")){
            if(term.indexOf("x") == 0){
                temp = 1;
            }else{
                temp = Double.parseDouble(term.substring(0,term.indexOf("x")));
            }
            if(term.contains("x^")){
                return new TermImp(temp,Integer.parseInt(term.substring(term.indexOf("^")+1)));
            }else{
                return new TermImp(temp, 1);
            }
        }else{
            return new TermImp(Double.parseDouble(term), 0);
        }
    }

    @Override
    public boolean equals(Object poly){
        PolynomialImp P2 = (PolynomialImp) poly;
        if(this.termsList.size() != P2.termsList.size()){
            return false;
        }
        for(int i = 0; i < this.termsList.size(); i++){

            if(this.termsList.get(i).getCoefficient() != P2.termsList.get(i).getCoefficient()){
                return false;
            }else if(this.termsList.get(i).getExponent() != P2.termsList.get(i).getExponent()){
                return false;
            }
        }
        return true;
    }

    @Override
    public Polynomial add(Polynomial P2) {
        PolynomialImp P2Imp = (PolynomialImp)P2; //Cast to be able to access termsList on P2.
        ArrayList<Term> returnTerms = new ArrayList<>();

        Iterator<Term> thisIterator = this.termsList.iterator();
        Iterator<Term> P2Iterator = P2Imp.termsList.iterator();

        String move = "both";
        Term tempTerm = null;
        Term P2tempTerm = null;

        //If the first term of one of the main.Polynomial is "0" returns the other main.Polynomial
        if(this.termsList.get(0).getCoefficient() == 0 && this.termsList.get(0).getExponent() == 0){
            return new PolynomialImp(termsToString(P2Imp.termsList, false));
        }else if(P2Imp.termsList.get(0).getCoefficient() == 0 && P2Imp.termsList.get(0).getExponent() == 0){
            return new PolynomialImp(termsToString(this.termsList, false));
        }

        while(thisIterator.hasNext()){
            //Checks which iterator to move based on if the exponent is equal or which is bigger
            switch(move){
                case "both":
                    tempTerm = thisIterator.next();
                    P2tempTerm = P2Iterator.next();
                    break;

                case "this":
                    tempTerm = thisIterator.next();
                    break;

                case "P2":
                    P2tempTerm = P2Iterator.next();
                    break;
            }
            //Adds the terms and moves both iterators
            if(tempTerm.getExponent() == P2tempTerm.getExponent()){
                Term sumTerm = new TermImp(tempTerm.getCoefficient() + P2tempTerm.getCoefficient(), tempTerm.getExponent());
                returnTerms.add(sumTerm);
                move = "both";

            //Adds the bigger term and iterates through that polynomial
            }else if (tempTerm.getExponent() > P2tempTerm.getExponent()){
                returnTerms.add(tempTerm);
                move = "this";
            }else{
                returnTerms.add(P2tempTerm);
                move = "P2";
            }
        }

        return new PolynomialImp(termsToString(returnTerms, false));
    }

    @Override
    public Polynomial subtract(Polynomial P2) {
        //multiply by -1 then add
        P2 = P2.multiply(-1);
        return this.add(P2);
    }

    @Override
    public Polynomial multiply(Polynomial P2) {
        PolynomialImp P2Imp = (PolynomialImp) P2;
        ArrayList<Term> newTerms = new ArrayList<>();

        for(Term P2Terms: P2Imp.termsList){
            for(Term thisTerms: this.termsList){
                Term temp = new TermImp(thisTerms.getCoefficient() * P2Terms.getCoefficient(), thisTerms.getExponent() + P2Terms.getExponent());

                //Checks newTerms for repeated terms with same exponents and adds them
                for(Term checkTerms: newTerms){
                    if(temp.getExponent() == checkTerms.getExponent()){
                        temp = new TermImp(temp.getCoefficient() + checkTerms.getCoefficient(), temp.getExponent());
                        newTerms.remove(checkTerms);
                    }
                }
                newTerms.add(temp);
            }
        }

        return new PolynomialImp(termsToString(newTerms, false));
    }

    @Override
    public Polynomial multiply(double c) {
        ArrayList<Term> newTerms = new ArrayList<>();

        for(Term terms: this.termsList){
            Term temp = new TermImp(terms.getCoefficient() * c, terms.getExponent());
            newTerms.add(temp);
        }

        return new PolynomialImp(termsToString(newTerms, false));
    }

    @Override
    public Polynomial derivative() {
        ArrayList<Term> newTerms = new ArrayList<>();

        for(Term terms: this.termsList){
            if(terms.getExponent() != 0){
                Term temp = new TermImp(terms.getCoefficient() * terms.getExponent(), terms.getExponent() - 1);
                newTerms.add(temp);
            }
        }

        return new PolynomialImp(termsToString(newTerms, false));
    }

    @Override
    public Polynomial indefiniteIntegral() {
        ArrayList<Term> newTerms = new ArrayList<>();
        for(Term terms: this.termsList){
            Term temp = new TermImp(terms.getCoefficient() / (terms.getExponent() + 1), terms.getExponent() + 1);
            newTerms.add(temp);
        }
        //Adds a "1" to represent "C" when integrating
        newTerms.add(new TermImp(1,0));

        return new PolynomialImp(termsToString(newTerms, false));
    }

    @Override
    public double definiteIntegral(double a, double b) {
        double evaluatedA = 0;
        double evaluatedB = 0;

        PolynomialImp integrated = (PolynomialImp)this.indefiniteIntegral();

        for (Term terms: integrated.termsList){
            evaluatedA += terms.evaluate(a);
            evaluatedB += terms.evaluate(b);
        }
        return evaluatedB - evaluatedA;
    }

    @Override
    public int degree() {
        int biggerExponent = 0;
        for(Term terms: this.termsList){
            if(terms.getExponent() > biggerExponent){
                biggerExponent  = terms.getExponent();
            }
        }
        return biggerExponent;
    }

    @Override
    public double evaluate(double x) {
        double result = 0;
        for(Term terms: this.termsList){
            result += terms.evaluate(x);
        }
        return result;
    }

    @Override
    public Iterator<Term> iterator() {

        return null;
    }
    @Override
    public String toString(){
        //Uses termsToString with formatting
        return termsToString(this.termsList, true);
    }

    //Non-member method to convert an Array of terms into a string
    //Example: An array of two terms [(3,2),(5,0)] returns the string "3x^2+5"
    //If format is true, returns the coefficients at two decimal places
    public static String termsToString(ArrayList<Term> termsArray, boolean format){
        String polyString = "";
        for(Term terms: termsArray){
            //If the coefficient is zero keeps iterating, if string is empty returns "0";
            if(terms.getCoefficient() != 0){
                if(terms.getExponent() != 0){
                    if(terms.getCoefficient() == 1){
                        polyString = polyString.concat("x");
                    }else{
                        //Decides if it will add a formatted value or the original value
                        if(format){
                            polyString = polyString.concat(String.format("%.2f", terms.getCoefficient()) + "x");
                        }else {
                            polyString = polyString.concat(terms.getCoefficient() + "x");
                        }
                    }
                    if(terms.getExponent() > 1){
                        polyString = polyString.concat("^" + terms.getExponent());
                    }
                } else{
                    if(format){
                        polyString = polyString.concat(String.format("%.2f", terms.getCoefficient()));
                    }else{
                        polyString = polyString.concat(String.valueOf(terms.getCoefficient()));
                    }
                }
                polyString = polyString.concat(" ");
            }
        }
        if(polyString.isEmpty()){
            return "0.00";
        }
        //Strips the ending space and switches spaces with a plus sign, this way terms are divided as they should be
        polyString = polyString.stripTrailing();
        polyString = polyString.replaceAll("\\s", "+");

        return polyString;
    }
}
