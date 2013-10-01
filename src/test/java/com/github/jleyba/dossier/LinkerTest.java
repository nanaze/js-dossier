package com.github.jleyba.dossier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(JUnit4.class)
public class LinkerTest {

  @Test
  public void testGetLink() {
    Path outputDir = Paths.get("");
    Config mockConfig = mock(Config.class);
    when(mockConfig.getOutput()).thenReturn(outputDir);
    DocRegistry mockRegistry = mock(DocRegistry.class);
    Linker linker = new Linker(mockConfig, mockRegistry);

    assertNull("No types are known", linker.getLink("goog.Foo"));

    Descriptor mockGoog = mock(Descriptor.class);
    when(mockRegistry.getType("goog")).thenReturn(mockGoog);
    when(mockGoog.getFullName()).thenReturn("goog");
    assertEquals("namespace_goog.html#goog.Foo", linker.getLink("goog.Foo"));
    assertEquals("namespace_goog.html#goog.Foo", linker.getLink("goog.Foo()"));

    Descriptor mockGoogFoo = mock(Descriptor.class);
    when(mockRegistry.getType("goog.Foo")).thenReturn(mockGoogFoo);
    when(mockGoogFoo.getFullName()).thenReturn("goog.Foo");
    assertEquals("namespace_goog_Foo.html", linker.getLink("goog.Foo"));
    assertEquals("namespace_goog_Foo.html", linker.getLink("goog.Foo()"));

    assertEquals("namespace_goog_Foo.html#goog.Foo.bar", linker.getLink("goog.Foo.bar"));
    assertEquals("namespace_goog_Foo.html#goog.Foo.bar", linker.getLink("goog.Foo.bar()"));
    assertEquals("namespace_goog_Foo.html#goog.Foo$bar", linker.getLink("goog.Foo#bar"));
    assertEquals("namespace_goog_Foo.html#goog.Foo$bar", linker.getLink("goog.Foo#bar()"));
  }

  @Test
  public void testGetSourcePath_sourceInfoNotFound() {
    Linker linker = new Linker(mock(Config.class), mock(DocRegistry.class));

    Descriptor mockDescriptor = mock(Descriptor.class);
    assertNull(linker.getSourcePath(mockDescriptor));
  }

  @Test
  public void testGetSourcePath_noLineNumber() {
    Path output = FileSystems.getDefault().getPath("foo/bar/baz");
    Path srcOutput = output.resolve("source");
    Path srcPrefix = FileSystems.getDefault().getPath("/alphabet/soup");

    Config mockConfig = mock(Config.class);
    when(mockConfig.getOutput()).thenReturn(output);
    when(mockConfig.getSourceOutput()).thenReturn(srcOutput);
    when(mockConfig.getSrcPrefix()).thenReturn(srcPrefix);

    Descriptor mockDescriptor = mock(Descriptor.class);
    when(mockDescriptor.getSource()).thenReturn("/alphabet/soup/a/b/c");

    Linker linker = new Linker(mockConfig, mock(DocRegistry.class));
    assertEquals("source/a/b/c.src.html", linker.getSourcePath(mockDescriptor));
  }

  @Test
  public void testGetSourcePath_withLineNumber() {
    Path output = FileSystems.getDefault().getPath("foo/bar/baz");
    Path srcOutput = output.resolve("source");
    Path srcPrefix = FileSystems.getDefault().getPath("/alphabet/soup");

    Config mockConfig = mock(Config.class);
    when(mockConfig.getOutput()).thenReturn(output);
    when(mockConfig.getSourceOutput()).thenReturn(srcOutput);
    when(mockConfig.getSrcPrefix()).thenReturn(srcPrefix);

    Descriptor mockDescriptor = mock(Descriptor.class);
    when(mockDescriptor.getSource()).thenReturn("/alphabet/soup/a/b/c");
    when(mockDescriptor.getLineNum()).thenReturn(123);

    Linker linker = new Linker(mockConfig, mock(DocRegistry.class));
    assertEquals("source/a/b/c.src.html#l122", linker.getSourcePath(mockDescriptor));
  }
}