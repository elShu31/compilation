.data
string_access_violation: .asciiz "Access Violation"
string_illegal_div_by_0: .asciiz "Illegal Division By Zero"
string_invalid_ptr_dref: .asciiz "Invalid Pointer Dereference"
.data
	global_i: .word 721
.data
	global_j: .word 721
.data
	global_p: .word 721
.data
	global_start: .word 721
.data
	global_end: .word 721
.data
	global_isPrime: .word 721
.data
	global_copyp: .word 721
.data
	global_copyisPrime: .word 721
.text
main:
	li $t0,2
	sw $t0,global_p
	li $t0,2
	sw $t0,global_start
	li $t0,100
	sw $t0,global_end
Label_1_start:
	lw $t2,global_p
	lw $t1,global_end
	li $t0,1
	add $t0,$t1,$t0
	blt $t2,$t0,Label_10_Lt_True
	li $t0,0
	j Label_11_Lt_End
Label_10_Lt_True:
	li $t0,1
Label_11_Lt_End:
	beq $t0,$zero,Label_0_end
	li $t0,2
	sw $t0,global_i
	li $t0,2
	sw $t0,global_j
	li $t0,1
	sw $t0,global_isPrime
Label_3_start:
	lw $t1,global_i
	lw $t0,global_p
	blt $t1,$t0,Label_12_Lt_True
	li $t0,0
	j Label_13_Lt_End
Label_12_Lt_True:
	li $t0,1
Label_13_Lt_End:
	beq $t0,$zero,Label_2_end
	li $t0,2
	sw $t0,global_j
Label_5_start:
	lw $t1,global_j
	lw $t0,global_p
	blt $t1,$t0,Label_14_Lt_True
	li $t0,0
	j Label_15_Lt_End
Label_14_Lt_True:
	li $t0,1
Label_15_Lt_End:
	beq $t0,$zero,Label_4_end
	lw $t0,global_p
	sw $t0,global_copyp
Label_7_start:
	lw $t1,global_i
	lw $t0,global_j
	mul $t1,$t1,$t0
	lw $t0,global_copyp
	beq $t1,$t0,Label_16_Eq_True
	li $t0,0
	j Label_17_Eq_End
Label_16_Eq_True:
	li $t0,1
Label_17_Eq_End:
	beq $t0,$zero,Label_6_end
	li $t0,0
	sw $t0,global_isPrime
	li $t0,0
	sw $t0,global_copyp
	j Label_7_start
Label_6_end:
	lw $t1,global_j
	li $t0,1
	add $t0,$t1,$t0
	sw $t0,global_j
	j Label_5_start
Label_4_end:
	lw $t1,global_i
	li $t0,1
	add $t0,$t1,$t0
	sw $t0,global_i
	j Label_3_start
Label_2_end:
	lw $t0,global_isPrime
	sw $t0,global_copyisPrime
Label_9_start:
	lw $t0,global_copyisPrime
	beq $t0,$zero,Label_8_end
	lw $t0,global_p
	move $a0,$t0
	li $v0,1
	syscall
	li $a0,32
	li $v0,11
	syscall
	li $t0,0
	sw $t0,global_copyisPrime
	j Label_9_start
Label_8_end:
	lw $t1,global_p
	li $t0,1
	add $t0,$t1,$t0
	sw $t0,global_p
	j Label_1_start
Label_0_end:
	li $v0,10
	syscall
