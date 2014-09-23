package com.fasterxml.jackson.dataformat.ini;

import java.io.*;
import java.net.URL;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.ini.impl.UTF8Reader;

public class IniFactory extends JsonFactory
{
    private static final long serialVersionUID = 1L;

    /**
     * Name used to identify the format this factory handles
     * (and returned by {@link #getFormatName()}
     */
    public final static String FORMAT_NAME = "INI";
    
    /**
     * Bitfield (set of flags) of all parser features that are enabled
     * by default.
     */
    final static int DEFAULT_CSV_PARSER_FEATURE_FLAGS = IniParser.Feature.collectDefaults();

    /**
     * Bitfield (set of flags) of all generator features that are enabled
     * by default.
     */
    final static int DEFAULT_CSV_GENERATOR_FEATURE_FLAGS = IniGenerator.Feature.collectDefaults();

    // could make it use Platform default too but...
    protected final static char[] DEFAULT_LF = { '\n' };

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */
    
    protected int _formatParserFeatures = DEFAULT_CSV_PARSER_FEATURE_FLAGS;

    protected int _formatGeneratorFeatures = DEFAULT_CSV_GENERATOR_FEATURE_FLAGS;

    /*
    /**********************************************************************
    /* Factory construction, configuration
    /**********************************************************************
     */

    /**
     * Default constructor used to create factory instances.
     * Creation of a factory instance is a light-weight operation,
     * but it is still a good idea to reuse limited number of
     * factory instances (and quite often just a single instance):
     * factories are used as context for storing some reused
     * processing objects (such as symbol tables parsers use)
     * and this reuse only works within context of a single
     * factory instance.
     */
    public IniFactory() { this(null); }

    public IniFactory(ObjectCodec oc) { super(oc); }

    protected IniFactory(IniFactory src, ObjectCodec oc)
    {
        super(src, oc);
        _formatParserFeatures = src._formatParserFeatures;
        _formatGeneratorFeatures = src._formatGeneratorFeatures;
    }
    
    @Override
    public IniFactory copy()
    {
        _checkInvalidCopy(IniFactory.class);
        return new IniFactory(this, null);
    }

    /*
    /**********************************************************
    /* Serializable overrides
    /**********************************************************
     */

    /**
     * Method that we need to override to actually make restoration go
     * through constructors etc.
     * Also: must be overridden by sub-classes as well.
     */
    @Override
    protected Object readResolve() {
        return new IniFactory(this, _objectCodec);
    }

    /*                                                                                       
    /**********************************************************                              
    /* Versioned                                                                             
    /**********************************************************                              
     */

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    /*
    /**********************************************************
    /* Capability introspection
    /**********************************************************
     */

    // no, not positional
    @Override
    public boolean requiresPropertyOrdering() {
        return false;
    }

    // No, we can't make use of char[] optimizations
    @Override
    public boolean canUseCharArrays() { return false; }

    /*
    /**********************************************************
    /* Format detection functionality
    /**********************************************************
     */
    
    @Override
    public String getFormatName() {
        return FORMAT_NAME;
    }

    @Override
    public MatchStrength hasFormat(InputAccessor acc) throws IOException
    {
        // !!! TBI
        return MatchStrength.INCONCLUSIVE;
    }

    @Override
    public boolean canUseSchema(FormatSchema schema) {
        return false;
    }

    /*
    /**********************************************************
    /* Configuration, parser settings
    /**********************************************************
     */

    /**
     * Method for enabling or disabling specified parser feature
     * (check {@link IniParser.Feature} for list of features)
     */
    public final IniFactory configure(IniParser.Feature f, boolean state)
    {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    /**
     * Method for enabling specified parser feature
     * (check {@link IniParser.Feature} for list of features)
     */
    public IniFactory enable(IniParser.Feature f) {
        _formatParserFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified parser features
     * (check {@link IniParser.Feature} for list of features)
     */
    public IniFactory disable(IniParser.Feature f) {
        _formatParserFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Checked whether specified parser feature is enabled.
     */
    public final boolean isEnabled(IniParser.Feature f) {
        return (_formatParserFeatures & f.getMask()) != 0;
    }

    /*
    /**********************************************************
    /* Configuration, generator settings
    /**********************************************************
     */

    /**
     * Method for enabling or disabling specified generator feature
     * (check {@link IniGenerator.Feature} for list of features)
     */
    public final IniFactory configure(IniGenerator.Feature f, boolean state) {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }


    /**
     * Method for enabling specified generator features
     * (check {@link IniGenerator.Feature} for list of features)
     */
    public IniFactory enable(IniGenerator.Feature f) {
        _formatGeneratorFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified generator feature
     * (check {@link IniGenerator.Feature} for list of features)
     */
    public IniFactory disable(IniGenerator.Feature f) {
        _formatGeneratorFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Check whether specified generator feature is enabled.
     */
    public final boolean isEnabled(IniGenerator.Feature f) {
        return (_formatGeneratorFeatures & f.getMask()) != 0;
    }
    
    /*
    /**********************************************************
    /* Overridden parser factory methods, 2.1
    /**********************************************************
     */

    @SuppressWarnings("resource")
    @Override
    public IniParser createParser(File f) throws IOException {
        return _createParser(new FileInputStream(f), _createContext(f, true));
    }

    @Override
    public IniParser createParser(URL url) throws IOException {
        return _createParser(_optimizedStreamFromURL(url), _createContext(url, true));
    }

    @Override
    public IniParser createParser(InputStream in) throws IOException {
        return _createParser(in, _createContext(in, false));
    }

    @Override
    public IniParser createParser(Reader r) throws IOException {
        return _createParser(r, _createContext(r, false));
    }

    @Override
    public IniParser createParser(String doc) throws IOException {
        return _createParser(new StringReader(doc), _createContext(doc, true));
    }
    
    @Override
    public IniParser createParser(byte[] data) throws IOException {
        return _createParser(data, 0, data.length, _createContext(data, true));
    }
    
    @Override
    public IniParser createParser(byte[] data, int offset, int len) throws IOException {
        return _createParser(data, offset, len, _createContext(data, true));
    }

    @Override
    public IniParser createParser(char[] data) throws IOException {
        return _createParser(data, 0, data.length, _createContext(data, true), false);
    }
    
    @Override
    public IniParser createParser(char[] data, int offset, int len) throws IOException {
        return _createParser(data, offset, len, _createContext(data, true), false);
    }

    /*
    /**********************************************************
    /* Overridden generator factory methods, 2.1+
    /**********************************************************
     */

    @SuppressWarnings("resource")
    @Override
    public IniGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException
    {
        // false -> we won't manage the stream unless explicitly directed to
        IOContext ctxt = _createContext(out, false);
        // [JACKSON-512]: allow wrapping with _outputDecorator
        if (_outputDecorator != null) {
            out = _outputDecorator.decorate(ctxt, out);
        }
        return _createGenerator(ctxt, _createWriter(out, JsonEncoding.UTF8, ctxt));
    }

    /**
     * This method assumes use of UTF-8 for encoding.
     */
    @Override
    public IniGenerator createGenerator(OutputStream out) throws IOException {
        return createGenerator(out, JsonEncoding.UTF8);
    }

    @SuppressWarnings("resource")
    @Override
    public IniGenerator createGenerator(Writer out) throws IOException
    {
        IOContext ctxt = _createContext(out, false);
        // [JACKSON-512]: allow wrapping with _outputDecorator
        if (_outputDecorator != null) {
            out = _outputDecorator.decorate(ctxt, out);
        }
        return _createGenerator(out, ctxt);
    }

    @SuppressWarnings("resource")
    @Override
    public IniGenerator createGenerator(File f, JsonEncoding enc) throws IOException
    {
        OutputStream out = new FileOutputStream(f);
        // Important: make sure that we always auto-close stream we create:
        IOContext ctxt = _createContext(out, false);
        // [JACKSON-512]: allow wrapping with _outputDecorator
        if (_outputDecorator != null) {
            out = _outputDecorator.decorate(ctxt, out);
        }
        return _createGenerator(ctxt, _createWriter(out, JsonEncoding.UTF8, ctxt));
    }

    /*
    /******************************************************
    /* Overridden internal factory methods
    /******************************************************
     */

    //protected IOContext _createContext(Object srcRef, boolean resourceManaged)

    /**
     * Overridable factory method that actually instantiates desired
     * parser.
     */
    @Override
    protected IniParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        BufferRecycler rec = _getBufferRecycler();
        return new IniParserBootstrapper(ctxt, rec, _objectCodec, in)
            .constructParser(_parserFeatures, _formatParserFeatures);
    }

    @Override
    protected IniParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException {
        BufferRecycler rec = _getBufferRecycler();
        return new IniParserBootstrapper(ctxt, rec, _objectCodec, data, offset, len)
            .constructParser(_parserFeatures, _formatParserFeatures);
    }

    /**
     * Overridable factory method that actually instantiates desired
     * parser.
     */
    @Override
    protected IniParser _createParser(Reader r, IOContext ctxt) throws IOException {
        return new IniParser(ctxt, _getBufferRecycler(), _parserFeatures, _formatParserFeatures,
                _objectCodec, r);
    }
    
    @Override
    protected IniParser _createParser(char[] data, int offset, int len, IOContext ctxt,
            boolean recyclable) throws IOException {
        return new IniParser(ctxt, _getBufferRecycler(), _parserFeatures, _formatParserFeatures,
                _objectCodec, new CharArrayReader(data, offset, len));
    }

    @Override
    protected IniGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        return _createGenerator(ctxt, out);
    }

    @Override
    protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException
    {
        if (enc == JsonEncoding.UTF8) {
            return new UTF8Writer(ctxt, out);
        }
        return new OutputStreamWriter(out, enc.getJavaName());
    }
    
    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */
    
    protected IniGenerator _createGenerator(IOContext ctxt, Writer out) throws IOException
    {
        IniGenerator gen = new IniGenerator(ctxt, _generatorFeatures, _formatGeneratorFeatures,
                _objectCodec, out);
        // any other initializations? No?
        return gen;
    }

    protected Reader _createReader(InputStream in, JsonEncoding enc, IOContext ctxt) throws IOException
    {
        // default to UTF-8 if encoding missing
        if (enc == null || enc == JsonEncoding.UTF8) {
            boolean autoClose = ctxt.isResourceManaged() || isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            return new UTF8Reader(ctxt, in, autoClose);
        }
        return new InputStreamReader(in, enc.getJavaName());
    }

    protected Reader _createReader(byte[] data, int offset, int len,
            JsonEncoding enc, IOContext ctxt) throws IOException
    {
        // default to UTF-8 if encoding missing
        if (enc == null || enc == JsonEncoding.UTF8) {
            return new UTF8Reader(null, data, offset, len);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(data, offset, len);
        return new InputStreamReader(in, enc.getJavaName());
    }
}
