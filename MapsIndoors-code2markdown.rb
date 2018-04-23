#!/System/Library/Frameworks/Ruby.framework/Versions/2.3/usr/bin/ruby


require 'optparse'
require 'ostruct'
require 'fileutils'


acceptedFileTypes = [".java"]


# Process commandline args:
$options = OpenStruct.new
optParser = OptionParser.new do |opt|
    opt.banner = "Usage: MapsIndoors-code2markdown.rb [options]"
    opt.on('-i', '--inputfolder FOLDER_PATH') { |o| $options[:inputfolder] = o }
    opt.on('-o', '--outputfolder FOLDER_PATH') { |o| $options[:outputfolder] = o }
    opt.on(      '--githubBaseUrl URL') { |o| $options[:githubBaseUrl] = o }
    opt.on_tail("-h", "--help", "Show this message") do
        puts opt
        exit 1
    end
    $options[:githubBaseUrl] = "https://github.com/MapsIndoors/MapsIndoorsIOS-Demo-Samples/blob/master/"
    $options[:outputfolder] = "markdown"
end
optParser.parse!

if $options.inputfolder.nil?
    puts optParser
    exit 1
end

puts $options.inputfolder
# Gather files to process from commandline args:
subFolders = Dir[$options.inputfolder + '/*'].select { |name| File.directory? name }
if subFolders.length == 0
    puts "No demo subfolders found - quitting"
    exit 1
end
puts  subFolders
files2process = []
$file2demoName = {}

subFolders.each do |demoFolder|

    filesInFolder = Dir[demoFolder + '/**/*.*'].select { |name| acceptedFileTypes.include? File.extname(name) }
    if filesInFolder.length == 0
        puts "Skipping " + demoFolder
    else
        puts "Generating markdown from " + demoFolder
        filesInFolder.each do |f|
            $file2demoName[f] = demoFolder
        end
        files2process.concat filesInFolder
    end

end
puts files2process

if files2process.length == 0
    puts "No files found - quitting"
    exit 1
end


# Helpers methods and global state
class ParsingState
    allStates = [ IGNORING = 'ignoring', COMMENT = 'comment', CODEBLOCK = 'codeblock' ]
end


$commentLineAccumulator = []
$codeLineAccumulator = []
$allOutput = []


def addCommentLine(l)
    $commentLineAccumulator.push( l )
end

def addCodeblockLine(l)
    $codeLineAccumulator.push( l )
end

def emitComment()
    # Drop leading and trailing empty lines
    lines2emit = $commentLineAccumulator.drop_while {|i| i.strip().length() == 0 }
    lines2emit = lines2emit.reverse.drop_while {|i| i.strip().length() == 0 }.reverse
    # Anything left? output lines
    if lines2emit.length > 0
        sep = lines2emit.length > 1 ? "\n" : ""
        $allOutput.push lines2emit.join(sep)
    end
    $commentLineAccumulator = []
end

def emitCodeblock()
    # Drop leading and trailing empty lines
    lines2emit = $codeLineAccumulator.drop_while {|i| i.strip().length() == 0 }
    lines2emit = lines2emit.reverse.drop_while {|i| i.strip().length() == 0 }.reverse
    # Anything left? output lines
    if lines2emit.length > 0
        numberOfLeadingSpaces = lines2emit.first[/\A */].size
        lines2emit = lines2emit.map { |l| l[numberOfLeadingSpaces..-1] }

        $allOutput.push "```\n"
        $allOutput.push lines2emit.join("")
        $allOutput.push "```"
    end
    $codeLineAccumulator = []
end

def emitFileOutput(filename)

    if $allOutput.length > 0
        bareName = File.basename(filename,File.extname(filename))
        demoName = $file2demoName[filename].split("/")[-1]
        fileName = (demoName + bareName).gsub(" ", "").downcase
        outputFilename = $options.outputfolder + "/" + fileName + ".md"
        exampleTitle = bareName.chomp("Controller").gsub(/[A-Z]/, ' \0')
        contents = []
        contents.concat $allOutput
        contents.push ""
        relativePath = filename.sub($options.inputfolder,"")[1..-1]
        githubLink = $options.githubBaseUrl + relativePath
        contents.push "[See the sample in " + File.basename(filename) +"](" + githubLink + ")"

        if not File.directory? $options.outputfolder
            FileUtils::mkdir_p $options.outputfolder
        end

        File.open(outputFilename, "w+") do |f|
            f.puts(contents)
        end

        $allOutput = []
    end
end


# Process all the files:
files2process.each do |f|
    lines = File.readlines(f)
    state = ParsingState::IGNORING
    allOutput = []

    lines.each do |l|

        orgLine = l
        l = l.strip()
        isStartComment = l.start_with?("/***")
        isEndComment = l.end_with?("***/")
        isEndCodeblock = l.start_with?("//") and l.end_with?("//")

        if isStartComment or isEndComment or (state == ParsingState::COMMENT)

            emitCodeblock()

            if isStartComment
                state = ParsingState::COMMENT
                l = l.sub!("/***","")
            end
            if isEndComment
                l = l.sub!("***/","")
            end
            addCommentLine l
        end

        if isEndComment
            emitComment()
            state = ParsingState::CODEBLOCK

        elsif state == ParsingState::CODEBLOCK
            if isEndCodeblock
                emitCodeblock()
                state = ParsingState::IGNORING
            else
                addCodeblockLine orgLine
            end
        end
    end

    if state == ParsingState::COMMENT
        emitComment
    elsif state == ParsingState::CODEBLOCK
        emitCodeblock
    end

    emitFileOutput f
end
