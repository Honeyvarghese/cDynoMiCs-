package iDynoOptimizer.Results.Feature.Temporal;

/**
 * Created by Chris on 4/16/2015.
 *
 *
 * A representation of the net incoming and net outgoing vectors (CONVERGENCE) for one grid site (this class doesn't know which grid site the vectors belong to)
 *
 */
public class InOut
    {
        private VectorSums outgoing;
        private VectorSums incoming;

        private VectorSums incOutDiff; //incoming - outgoing
        private VectorSums incOutSum; //incoming + outgoing;


        private void subtractOutgoing(VectorSums o)
        {
            this.outgoing.subtract(o);
        }
        private void subtractInComing(VectorSums o)
        {
            this.incoming.subtract(o);
        }


        public InOut()
            {
                outgoing = new VectorSums();
                incoming = new VectorSums();
            }

        public InOut(VectorSums out, VectorSums in)
            {
                this.outgoing = out;
                this.incoming = in;
            }

        public VectorSums getOutgoing()
            {
                return outgoing;
            }

        public VectorSums getIncoming()
            {
                return incoming;
            }

        public void add(InOut io)
            {
                add(io.outgoing, io.incoming);
            }

        public void add(VectorSums outgoing, VectorSums incoming)
            {
                this.outgoing.add(outgoing);
                this.incoming.add(incoming);
            }

        public void addOutgoing(VectorSums o)
            {
                this.outgoing.add(o);
            }
        public void addIncoming(VectorSums o)
            {
                this.incoming.add(o);
            }



        public void subtract(InOut o)
        {
            subtractOutgoing(o.getOutgoing());
            subtractInComing(o.getIncoming());
        }

        public void divide(double divisor)
            {
                outgoing.divide(divisor);
                incoming.divide(divisor);
            }


        public VectorSums calcIncOutSum()
        {
            incOutSum = new VectorSums();
            incOutSum.add(
                    Vector.add(incoming.getV(), outgoing.getV()),
                    Vector.add(incoming.getScalarConvDiv(), outgoing.getScalarConvDiv()),
                    Vector.add(incoming.getvMom(), outgoing.getvMom()),
                    Vector.add(incoming.getMomScalarConvDiv(), outgoing.getMomScalarConvDiv()),
                    incoming.getMag() + outgoing.getMag(),
                    incoming.getMagMom() + outgoing.getMagMom(),
                    incoming.getMass() + outgoing.getMass(),
                    incoming.getCount() + outgoing.getCount());

            return incOutSum;
        }

        public VectorSums calcIncOutDiff()
            {

                incOutDiff = new VectorSums();
                incOutDiff.add(
                        Vector.subtract(incoming.getV(), outgoing.getV()),
                        Vector.subtract(incoming.getScalarConvDiv(), outgoing.getScalarConvDiv()),
                        Vector.subtract(incoming.getvMom(), outgoing.getvMom()),
                        Vector.subtract(incoming.getMomScalarConvDiv(), outgoing.getMomScalarConvDiv()),
                        incoming.getMag() - outgoing.getMag(),
                        incoming.getMagMom() - outgoing.getMagMom(),
                        incoming.getMass() - outgoing.getMass(),
                        incoming.getCount() - outgoing.getCount());




                return incOutDiff;

            }

        public VectorSums getIncOutDiff()
            {
                calcIncOutDiff();
                return incOutDiff;
            }


        public VectorSums getIncOutSum()
        {
            calcIncOutSum();
            return incOutSum;
        }




    }
