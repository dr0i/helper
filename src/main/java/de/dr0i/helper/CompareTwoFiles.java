package de.dr0i.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets.SetView;

/**
 * This compares all elements of file A with those of file B. All elements which
 * are in A but not in B are printed to standard out. Elements which are part of
 * B but not of A are ignored.
 * 
 * This is nearly as fast as it takes to load the files as computing is done in
 * memory. So - have enough memory. Is all you need.
 * 
 * @author dr0i
 *
 */
public class CompareTwoFiles {

	public static void main(String... args) {
		File fileA = new File("./CompareTwoFiles/verbund.idn");
		File fileB = new File("./CompareTwoFiles/lobid.idn");
		if (args.length == 2) {
			fileA = new File(args[0]);
			fileB = new File(args[1]);
		}
		compareFiles(fileA, fileB);
	}

	/**
	 * @param generatedSet
	 *            the generated data as set
	 * @param testFile
	 *            expected data as file
	 */
	public static void compareFiles(final File fileA, final File fileB) {
		TreeSet<String> setA = asNormalizedSet(fileToStream(fileA), "");
		TreeSet<String> setB = asNormalizedSet(fileToStream(fileB), "");
		SetView<String> difference = com.google.common.collect.Sets.difference(setA, setB);
		difference.forEach(System.out::println);
	}

	@SuppressWarnings("resource")
	private static Stream<String> fileToStream(final File file) {
		Stream<String> stream = null;
		InputStream is;
		try {
			is = new FileInputStream(file);
			stream = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return stream;
	}

	private static TreeSet<String> asNormalizedSet(Stream<String> unnormalizedStream, String patternToRemove) {
		return new TreeSet<>(unnormalizedStream.flatMap(s -> Stream.of(s.replaceAll(patternToRemove, "")))
				.collect(Collectors.toList()));
	}
}
