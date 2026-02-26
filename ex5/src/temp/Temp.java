/***********/
/* PACKAGE */
/***********/
package temp;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class Temp {
	private int serial = 0;

	public Temp(int serial) {
		this.serial = serial;
	}

	public int getSerialNumber() {
		return serial;
	}

	public void setSerialNumber(int serial) {
		this.serial = serial;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Temp temp = (Temp) o;
		return serial == temp.serial;
	}

	@Override
	public int hashCode() {
		return serial;
	}
}
