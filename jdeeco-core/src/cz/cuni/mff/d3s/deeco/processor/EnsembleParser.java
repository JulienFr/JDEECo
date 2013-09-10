package cz.cuni.mff.d3s.deeco.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Selector;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.invokable.ParameterizedMethod;
import cz.cuni.mff.d3s.deeco.invokable.ParameterizedSelectorMethod;
import cz.cuni.mff.d3s.deeco.invokable.SchedulableEnsembleProcess;
import cz.cuni.mff.d3s.deeco.invokable.memberships.AbstractMembershipMethod;
import cz.cuni.mff.d3s.deeco.invokable.memberships.MemberMembershipMethod;
import cz.cuni.mff.d3s.deeco.invokable.memberships.MembersetMembershipMethod;
import cz.cuni.mff.d3s.deeco.invokable.memberships.SelectedMembersMembershipMethod;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.path.grammar.ParseException;
import cz.cuni.mff.d3s.deeco.path.grammar.PathGrammar;
import cz.cuni.mff.d3s.deeco.scheduling.ProcessPeriodicSchedule;
import cz.cuni.mff.d3s.deeco.scheduling.ProcessSchedule;


/**
 * Parser class for ensemble definitions.
 * 
 * @author Michal Kit
 *
 */
public class EnsembleParser {

	/**
	 * Static function used to extract {@link SchedulableEnsembleProcess}
	 * instance from the class definition
	 * 
	 * @param c
	 *            class to be parsed for extraction
	 * @param km
	 *            {@link KnowledgeManager} instance that is used for knowledge
	 *            repository communication
	 * @return a {@link SchedulableEnsembleProcess} instance extracted from the
	 *         class definition
	 */
	public static SchedulableEnsembleProcess extractEnsembleProcess(Class<?> c)
			throws ParseException {
		// TODO: put names into the exception strings

		if (!isEnsembleDefinition(c)) {
			throw new ParseException("The class " + c.getName()
					+ " is not an ensemble definition.");
		}

		assert (c != null);
		// check methodEnsMembership
		final Method methodEnsMembership = AnnotationHelper.getAnnotatedMethod(
				c, Membership.class);
		// check methodEnsMembership existence
		if (methodEnsMembership == null) {
			throw new ParseException(
					"The ensemble definition does not define a membership function");
		}
		// check methodEnsMembership ParameterizedMethod existence
		ParameterizedMethod pm = ParserHelper
				.extractParametrizedMethod(methodEnsMembership);

		if (pm == null) {
			throw new ParseException(
					"Malformed membership function definition.");
		}
	
		// check methodEnsMembership return types
		Class<?> returnType = methodEnsMembership.getReturnType();
		if (!(returnType.equals(Boolean.class) || returnType.equals(boolean.class))){
			throw new ParseException(
					"The return type must be always the Boolean primitive java type");
		}
		// XXX: stronger parsing of ensembles, by crossing different cases
		// check methodEnsMembership parameter annotations on multiplicity (=1) and value
		Annotation[][] annotations = methodEnsMembership.getParameterAnnotations();
		// lists of annotations per parameter type
		List<Annotation> memberParameterAnnotations = new ArrayList<Annotation> ();
		List<Annotation> membersParameterAnnotations = new ArrayList<Annotation> ();
		List<Annotation> selectorsParameterAnnotations = new ArrayList<Annotation> ();
		List<Annotation> membersetParameterAnnotations = new ArrayList<Annotation> ();
		// requirement : there should be parameters
		if (annotations[0].length == 0){
			throw new ParseException(
					"A membership parameter must have in/inOut/out annotation");
		}
		// differentiates the parameter annotations
		// * just member parameters then MemberMembershipMethod
		// * just members parameters then MembersMembershipMethod
		// * members and selectors parameters then SelectedMembersMembershipMethod
		for (int i = 0; i < annotations.length; i++){
			Annotation a = annotations[i][0];
			// requirement : only one annotation per parameter
			if (annotations[i].length > 1){
				throw new ParseException(
						"A membership parameter should have only one in or inOut or out annotation");
			}
			// registers the member/members annotations
			if (a.annotationType().equals(In.class)){
				// WARNING: the startsWith can set priority cases between the grammar keywords !
				// memberset
				if (((In) a).value().startsWith(PathGrammar.MEMBERSET)){
					membersetParameterAnnotations.add(a);
				// members
				}else if (((In) a).value().startsWith(PathGrammar.MEMBERS)){
					membersParameterAnnotations.add(a);
				// member
				}else if (((In) a).value().startsWith(PathGrammar.MEMBER)){
					memberParameterAnnotations.add(a);
				}
				
			}else if(a.annotationType().equals(Selector.class)){
				selectorsParameterAnnotations.add(a);
			}else{
				throw new ParseException(
						"A membership parameter has more than one in/inOut/out annotation");
			}
		}
		// there should not be member annotations mixed with selectors annotations
		if (memberParameterAnnotations.size() > 0 && selectorsParameterAnnotations.size() > 0){
			throw new ParseException(
				"The membership function can't have both member paths and selectors paths altogether");
		}
		// no selector when using the memberset annotation
		if (membersetParameterAnnotations.size() > 0 && (selectorsParameterAnnotations.size() > 0 || membersParameterAnnotations.size() > 0)){
			throw new ParseException(
				"The membership function can't have both memberset paths and selectors/members paths altogether");
		}
		// instantiate the membership according to the returnType class and the EEnsembleParty identifier
		AbstractMembershipMethod<?> membership = null;
		if (memberParameterAnnotations.size() > 0){
			membership = new MemberMembershipMethod(pm);
		}else if (membersParameterAnnotations.size() > 0){
			// TODO : checks the number of selectors according to the members annotations
			// Extracts the selectors from the function parameters and imposes the hierarchy by group identifiers
			pm = ParserHelper.extractParametrizedSelectorMethod(methodEnsMembership);
			membership = new SelectedMembersMembershipMethod((ParameterizedSelectorMethod) pm);
		}else if (membersetParameterAnnotations.size() > 0){
			membership = new MembersetMembershipMethod(pm);
		}
		
		// knowledge exchange parsing
		final Method knowledgeExchangeMethod = AnnotationHelper
				.getAnnotatedMethod(c, KnowledgeExchange.class);

		if (knowledgeExchangeMethod == null) {
			throw new ParseException(
					"The ensemble definition does not define a knowledge exchange function");
		}

		ParameterizedMethod knowledgeExchange = null;
		// simple extraction of in/inout/out inline parameters from the knowledge exchange method
		if (membership instanceof MemberMembershipMethod || membership instanceof MembersetMembershipMethod){
			knowledgeExchange = ParserHelper.extractParametrizedMethod(knowledgeExchangeMethod);
		// the membership selectors poopulate explicitly the knowledge exchange selectors
		}else if (membership instanceof SelectedMembersMembershipMethod){
			knowledgeExchange = ParserHelper.extractImplicitParametrizedSelectorMethod(knowledgeExchangeMethod, ((ParameterizedSelectorMethod) pm).selectors);
		}
		
		if (knowledgeExchange == null) {
			throw new ParseException(
					"Malformed knowledge exchange function definition. " + c);
		}

		// Look up scheduling
		ProcessSchedule scheduling = null;

		final ProcessSchedule periodicSchedule = ScheduleHelper
				.getPeriodicSchedule(AnnotationHelper.getAnnotation(
						PeriodicScheduling.class, knowledgeExchangeMethod.getAnnotations()));
		if (periodicSchedule != null) {
			scheduling = periodicSchedule;
		}

		if (scheduling == null) {
			// not periodic
			final ProcessSchedule triggeredSchedule = ScheduleHelper
					.getTriggeredSchedule(
							knowledgeExchangeMethod.getParameterAnnotations(),
							knowledgeExchange.in, knowledgeExchange.inOut);

			if (triggeredSchedule != null) {
				scheduling = triggeredSchedule;
			}
		}

		if (scheduling == null) {
			// No scheduling specified by annotations, using defaults
			scheduling = new ProcessPeriodicSchedule();
		}
		
		return new SchedulableEnsembleProcess(null, scheduling, membership,
					knowledgeExchange, null);
	}

	/**
	 * Checkes whether the given class is an ensemble definitions.
	 * 
	 * @param clazz class to be checked
	 * @return True if the given class is an ensemble definition. False otherwise.
	 */
	public static boolean isEnsembleDefinition(Class<?> clazz) {
		return clazz != null && Ensemble.class.isAssignableFrom(clazz);
	}
}
