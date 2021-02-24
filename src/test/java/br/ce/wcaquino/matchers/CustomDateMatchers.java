package br.ce.wcaquino.matchers;

import java.util.Calendar;
import java.util.Date;

import br.ce.wcaquino.utils.DataUtils;

public class CustomDateMatchers {

	public static DiaSemanaMatcher caiEm(Integer diaSemana) {
		return new DiaSemanaMatcher(diaSemana);
	}
	
	public static DiaSemanaMatcher caiNumaSegunda() {
		return new DiaSemanaMatcher(Calendar.MONDAY);
	}
	
	public static DateMatcher ehHoje() {
		Date hoje = new Date();
		return new DateMatcher(hoje);
	}
	
	public static DateMatcher ehAmanha() {
		Date hoje = new Date();
		return new DateMatcher(DataUtils.adicionarDias(hoje, 1));
	}
}
