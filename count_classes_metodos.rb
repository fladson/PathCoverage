require 'set'
last_pkg = "src"
klasses = Set.new()
mets = Set.new()
File.foreach("callEntries.txt") do |line|
	line.gsub(/(?<=src.)(.*?)(?=\.)/) { |match| klasses.add(match)}
	line.gsub(/(?<=\.)(\w*?)(?=\()/) { |match| mets.add(match)}

end

puts "CLASSES:"
klasses.each do |kl|
	puts kl
end
puts klasses.size

puts "===========================\nMETODOS:"
mets.each do |m|
	puts m
end
puts mets.size