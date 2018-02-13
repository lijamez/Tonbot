package net.tonbot.core.request

import spock.lang.Specification

class RequestMapperTest extends Specification {
	
	def "foo"() {
		given:
		RequestMapper mapper = new RequestMapper();
		
		when:
		SampleRequest targetObj = mapper.map("  123 -6000 8 12.34   -.1   true blah\" \"some\\\" string\" deny extra ", SampleRequest.class);
		
		then:
		System.out.println(targetObj);
		true
	}

}
