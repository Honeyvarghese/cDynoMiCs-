package iDynoOptimizer.Global;

/**
 * Created by chris on 11/17/2015.
 */
public class MyPrinter {


    private static MyPrinter myPrinter;

    private boolean tier1On;
    private boolean tier2On;


    /*
    Turns printing on or off for important information,
    including ALL errors (System.err) and basic output indicating the important events of search
     */
    public void setTier1On(boolean tier1On)
    {
        this.tier1On = tier1On;
    }

    /*
    Turns printing on or off for less important information,
    includes stuff printed for debugging purposes and some other less important stuff
     */
    public void setTier2On(boolean tier2On)
    {
        this.tier2On = tier2On;
    }






    public static MyPrinter Printer()
    {
        if(myPrinter == null) myPrinter = new MyPrinter();

        return myPrinter;
    }


    private MyPrinter()
    {
        tier1On = true;
        tier2On = false;
    }



    public void printTier1ln(String toPrint, boolean forcePrint)
    {
        if(forcePrint || tier1On)System.out.println(toPrint);

    }

    public void printTier1ln(String toPrint)
    {
        printTier1ln(toPrint, false);
    }


    public void printTier1ln(Number toPrint, boolean forcePrint)
    {
        printTier1ln(String.valueOf(toPrint), forcePrint);
    }
    public void printTier1ln(Number toPrint)
    {
        printTier1ln(String.valueOf(toPrint), false);
    }






    public void printTier2ln(String toPrint, boolean forcePrint)
    {
        if(forcePrint || tier2On)System.out.println(toPrint);

    }

    public void printTier2ln(String toPrint)
    {
        printTier2ln(toPrint, false);
    }





    public void printErrorln(String toPrint, boolean forcePrint)
    {
        if(forcePrint || tier1On)System.err.println(toPrint);

    }

    public void printErrorln(String toPrint)
    {
        printErrorln(toPrint, false);
    }


}
