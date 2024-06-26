package io.github.seccoding.excel.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSheet {

	public String value() default "Sheet1";
	
	public int startRow() default 1;
	
	public boolean useTitle() default false;
	
}
