package net.tonbot.core.request;

import lombok.Data;

@Data
public class SampleRequest {

	@Param(index = 0)
	public int intValue;
	
	@Param(index = 1)
	public long longValue;
	
	@Param(index = 2)
	public short shortValue;
	
	@Param(index = 3) 
	public double doubleValue;
	
	@Param(index = 4) 
	public float floatValue;
	
	@Param(index = 5) 
	public boolean booleanValue;
	
	@Param(index = 6)
	public String easyString;
	
	@Param(index = 7)
	public String multiWordString;
	
	@Param(index = 8) 
	public SampleEnum enumValue;
	
	//TODO: More discord-specific types like mentions.
}
