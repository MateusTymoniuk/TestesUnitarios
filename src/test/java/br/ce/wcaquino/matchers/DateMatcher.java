package br.ce.wcaquino.matchers;

import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DateMatcher extends TypeSafeMatcher<Date> {
	
	private Date data;
	
	public DateMatcher(Date data) {
		this.data = data;
	}

	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean matchesSafely(Date data) {
		return DataUtils.isMesmaData(this.data, data);
	}

}
