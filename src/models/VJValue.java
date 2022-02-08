package models;

public class VJValue {

        public int intvalue;
        public float floatvalue;
        public int index;
        // The k depth of this VJValue.
        public int k;

        public VJValue() {}
        public VJValue(VJValue value)
        { this.floatvalue =  value.floatvalue; this.intvalue = value.intvalue; this.index = value.index; }
        public VJValue(float fl)
        { this.floatvalue =  fl; this.intvalue = (int) floatvalue; this.index = 0; }
        public void setIndex(int index) { this.index = index; }
        public int getIndex() { return index; }
        public int getIntValue() { return intvalue; }
        public float get() { return floatvalue; }
        public String toString() { return ""+floatvalue; }

}
