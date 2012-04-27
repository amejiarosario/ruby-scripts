# encoding: UTF-8

require 'set'
require 'pp'

INSERT_PATTERN = /insert\W+into\W+(\w+)\(([^)]*)\)\W*values\W*\(([^;]*)\)/i

@data_path = "matrix"
@data_file = "#{@data_path}/matrix_resistors.csv"
@query_file = "#{@data_path}/matrix_resistors.sql"
@output_file = "#{@data_path}/matrix_resistors_processed.sql"
DEBUG_LIMIT = 3
TRACE = true
DEBUG = true

class String
	def csv_spliter(separator)
		# check for parenthesis
		# omit commas inside the parenthesis
		buffer = ""
		array = []
		self.each_char |c|
			case c
			when '('
				while 
			when separator[0]
				buffer += c
			end
			if c != separator[0]
				buffer += c
			end
		end
		#self.split(separator)
	end
end

def csv_2_hash (file) 
	hash = nil
	header = nil
	times = 0
	File.open(file) do |f|
		while line = f.gets do
			if hash.nil?
				#header
				hash = {}
				header = line.split(",")
				header.each do |h|
					hash[h] = []
				end
			else
				line.split(",").each.with_index do |r,i|
					hash[header[i]] << r.to_s.strip #.gsub(/[\x80-\xff]/,"") # TODO refine
				end
			end
			times += 1
			break if DEBUG and times > DEBUG_LIMIT # TODO 
		end
	end
	hash
end

def hash_2_sqlinsert (hash)
	"INSERT INTO #{hash[:name]} (#{hash.keys[1..-1].map{|k| k.to_s}.join(", ")}) VALUES (#{hash.values[1..-1].map{|v| v.to_s}.join(", ")});\n"
end

def main
	set = Set.new
	hash = csv_2_hash @data_file
	
	p "===============" if DEBUG
	#puts hash.inspect if DEBUG
	pp hash if DEBUG
	p "===============" if DEBUG
	
	path = @query_file
	puts "reading... #{path}"
	
	File.open(path) do |f|
		# Get SQL stmt
		times = 0
		while line = f.gets do
			#puts line
			table = column_names = values = ""
			match = INSERT_PATTERN.match line
			if match
				table = match[1]
				column_names = match[2].csv_spliter(",") unless match[2].nil?
				values = match[3].csv_spliter(",") unless match[3].nil?
				
				# make a hash t with the key => value of the insert statement
				t={}
				t[:name] = table
				column_names.each.with_index do |c,i|
					t[c] = values[i].to_s.strip
				end
				puts "\n>> original: #{t.inspect}" if DEBUG
				
				#
				# replace the values if the hash of values have a column for it.
				#
				hash.values[0].count.times do |i|
					t.each_key do |k|
						unless hash[k].nil?
							t[k] = "'#{hash[k][i]}'" if hash[k][i]
							#puts "<#{k}> updated." if TRACE
						end
					end
					puts "\n>> changed: #{t.inspect}\n<<<<" if DEBUG
					set.add hash_2_sqlinsert(t)
				end
				#
				#
				#
				
				times += 1
				
			else
				puts "DIDN'T match #{line}"
			end

			#puts "table=#{table}\ncolumns=#{column_names}\nvalues=#{values}\n\n"
			break if DEBUG and times >  DEBUG_LIMIT
		end
#=begin		
		File.open(@output_file,'w') do |out|
			set.each do |v|
				out.write v
			end
		end
#=end
	end
end


main
